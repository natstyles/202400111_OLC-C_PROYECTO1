package com.eli.ide.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;


public class IdeFrame extends JFrame {

    private final JTextArea editor = new JTextArea();
    private final JTextArea console = new JTextArea();
    private final JTable outputTable;
    private File currentFile = null;
    private boolean dirty = false; //hay cambios sin guardar
    private boolean suppressDirty = false;
    private DocumentListener dirtyListener;
    private Document attachedDocument; //saber a cuál documento está pegado el listener


    public IdeFrame() {
        super("ELI-NOSQL");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (confirmDiscardIfNeeded()) dispose();
            }
        });

        setSize(1100, 700);
        setLocationRelativeTo(null);

        setJMenuBar(buildMenuBar());

        //Panel de editor
        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JScrollPane editorScroll = new JScrollPane(editor);
        editorScroll.setBorder(new TitledBorder("Editor"));
        installDirtyTracking();
        updateTitle();


        //Consola
        console.setEditable(false); //No puedan escribir
        console.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JScrollPane consoleScroll = new JScrollPane(console);
        consoleScroll.setBorder(new TitledBorder("Consola"));

        //Salida
        outputTable = new JTable(new DefaultTableModel(
                new Object[]{"Campo", "Valor"}, 0
        ));
        JScrollPane outputScroll = new JScrollPane(outputTable);
        outputScroll.setBorder(new TitledBorder("Output"));

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, consoleScroll, outputScroll);
        rightSplit.setResizeWeight(0.55);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorScroll, rightSplit);
        mainSplit.setResizeWeight(0.6);

        add(mainSplit, BorderLayout.CENTER);

        updateTitle();
    }

    //barra superior
    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("Archivo");

        JMenuItem nuevo = new JMenuItem("Nuevo");
        nuevo.addActionListener(e -> newFile());

        JMenuItem abrir = new JMenuItem("Abrir...");
        abrir.addActionListener(e -> openFile());

        JMenuItem guardar = new JMenuItem("Guardar");
        guardar.addActionListener(e -> saveFile());

        JMenuItem guardarComo = new JMenuItem("Guardar como...");
        guardarComo.addActionListener(e -> saveFileAs());

        file.add(nuevo);
        file.add(abrir);
        file.add(guardar);
        file.add(guardarComo);

        JMenu run = new JMenu("Ejecutar");
        JMenuItem ejecutar = new JMenuItem("Run");
        ejecutar.addActionListener(e -> runDemo());
        run.add(ejecutar);

        bar.add(file);
        bar.add(run);

        return bar;
    }

    private void installDirtyTracking() {
        if (attachedDocument != null && dirtyListener != null) {
            attachedDocument.removeDocumentListener(dirtyListener);
        }

        dirtyListener = new DocumentListener() {
            private void changed() {
                if (suppressDirty) return;
                if (!dirty) {
                    dirty = true;
                    updateTitle();
                }
            }

            @Override public void insertUpdate(DocumentEvent e) { changed(); }
            @Override public void removeUpdate(DocumentEvent e) { changed(); }
            @Override public void changedUpdate(DocumentEvent e) { changed(); }
        };

        attachedDocument = editor.getDocument();
        attachedDocument.addDocumentListener(dirtyListener);
    }

    private void updateTitle() {
        String name = (currentFile == null) ? "Nuevo archivo" : currentFile.getName();
        setTitle("ELI NOSQL - " + (dirty ? "*" : "") + name);
    }

    private boolean confirmDiscardIfNeeded() {
        if (!dirty) return true;

        int option = JOptionPane.showConfirmDialog(
                this,
                "Tienes cambios sin guardar. ¿Deseas guardarlos?",
                "Cambios sin guardar",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) return false;
        if (option == JOptionPane.YES_OPTION) return saveFile(); // si cancela o falla, no continúa
        return true; // NO = descartar cambios
    }

    //Métodos
    private void newFile() {
        if (!confirmDiscardIfNeeded()) return;

        suppressDirty = true;
        try {
            editor.setText("");
        } finally {
            suppressDirty = false;
        }

        installDirtyTracking();
        currentFile = null;
        dirty = false;
        updateTitle();
    }

    private void openFile() {
        if (!confirmDiscardIfNeeded()) return;

        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(selected))) {

                suppressDirty = true;
                try {
                    editor.read(reader, null);
                } finally {
                    suppressDirty = false;
                }

                installDirtyTracking();
                currentFile = selected;
                dirty = false;
                updateTitle();

            } catch (IOException ex) {
                suppressDirty = false;
                JOptionPane.showMessageDialog(this, "Error al abrir archivo: " + ex.getMessage());
            }
        }
    }

    private boolean saveFile() {
        if (currentFile == null) return saveFileAs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
            editor.write(writer);
            dirty = false;
            updateTitle();
            return true;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar archivo: " + ex.getMessage());
            return false;
        }
    }

    private boolean saveFileAs() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            return saveFile();
        }
        return false;
    }

    private void runDemo() {
        console.append(">> Ejecutando (demo)...\n");
        console.append(">> (En Fase 1 aquí invocamos JFlex+CUP)\n\n");

        DefaultTableModel model = (DefaultTableModel) outputTable.getModel();
        model.setRowCount(0);
        model.addRow(new Object[]{"status", "ok"});
        model.addRow(new Object[]{"rows", 0});
    }
}

package com.eli.ide.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;

public class IdeFrame extends JFrame {

    private final JTextArea editor = new JTextArea();
    private final JTextArea console = new JTextArea();
    private final JTable outputTable;
    private File currentFile = null;

    public IdeFrame() {
        super("ELI-NOSQL");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        setJMenuBar(buildMenuBar());

        //Panel de editor
        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JScrollPane editorScroll = new JScrollPane(editor);
        editorScroll.setBorder(new TitledBorder("Editor"));

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

    //Métodos
    private void newFile() {
        editor.setText("");
        currentFile = null;
        setTitle("ELI NOSQL - Nuevo archivo");
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                editor.read(reader, null);
                setTitle("ELI NOSQL - " + currentFile.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al abrir archivo");
            }
        }
    }

    private void saveFile() {
        if (currentFile == null) {
            saveFileAs();
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
            editor.write(writer);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar archivo");
        }
    }

    private void saveFileAs() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            saveFile();
            setTitle("ELI NOSQL - " + currentFile.getName());
        }
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

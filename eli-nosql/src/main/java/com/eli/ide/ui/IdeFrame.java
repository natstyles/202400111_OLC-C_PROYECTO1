package com.eli.ide.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import java.awt.Desktop;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import java_cup.runtime.Symbol;
import analizadores.sym;

public class IdeFrame extends JFrame {

    private final JTextArea editor = new JTextArea();
    private final JTextArea console = new JTextArea();
    private final JTable tokensTable;
    private final JTable errorsTable;
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

        //Salida (Tokens)
        //TOKENS
        tokensTable = new JTable(new DefaultTableModel(
                new Object[]{"#", "Lexema", "Tipo", "Línea", "Columna"}, 0
        ));
        JScrollPane tokensScroll = new JScrollPane(tokensTable);

        //ERROR
        errorsTable = new JTable(new DefaultTableModel(
                new Object[]{"#", "Tipo", "Descripción", "Línea", "Columna"}, 0
        ));
        JScrollPane errorsScroll = new JScrollPane(errorsTable);

        //Tabs dentro de Output
        JTabbedPane outputTabs = new JTabbedPane();
        outputTabs.addTab("Tokens", tokensScroll);
        outputTabs.addTab("Errores", errorsScroll);

        JScrollPane outputScroll = new JScrollPane(outputTabs);
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

        JMenuItem abrir = new JMenuItem("Abrir.");
        abrir.addActionListener(e -> openFile());

        JMenuItem guardar = new JMenuItem("Guardar");
        guardar.addActionListener(e -> saveFile());

        JMenuItem guardarComo = new JMenuItem("Guardar como.");
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

        JMenu reportes = new JMenu("Reportes");

        JMenuItem html = new JMenuItem("Generar HTML");
        html.addActionListener(e -> generarHTMLReport());

        reportes.add(html);

        bar.add(reportes);

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

    //llena la tabla con la lista de tokens + errores léxicos
    private void fillTokensAndLexErrors(String input) {
        DefaultTableModel tok = (DefaultTableModel) tokensTable.getModel();
        DefaultTableModel err = (DefaultTableModel) errorsTable.getModel();
        tok.setRowCount(0);
        err.setRowCount(0);

        int nTok = 1;
        int nErr = 1;

        try {
            analizadores.Lexico lex = new analizadores.Lexico(new StringReader(input));

            while (true) {
                Symbol s = lex.next_token();
                if (s == null) break;
                if (s.sym == sym.EOF) break;

                String tokenName = (s.sym >= 0 && s.sym < sym.terminalNames.length)
                        ? sym.terminalNames[s.sym]
                        : ("SYM_" + s.sym);

                String lexema = (s.value == null) ? "" : s.value.toString();
                int linea = s.left;
                int columna = s.right;

                //Si es ERROR léxico → SOLO tabla ERRORES
                if ("ERROR".equals(tokenName)) {
                    err.addRow(new Object[]{
                            nErr++,
                            "Léxico",
                            lexema,
                            linea,
                            columna
                    });
                    continue;
                }

                //Si no es error → tabla TOKENS
                tok.addRow(new Object[]{
                        nTok++,
                        lexema,
                        tipoEnunciado(tokenName),
                        linea,
                        columna
                });
            }

        } catch (Exception ex) {
            err.addRow(new Object[]{
                    nErr++,
                    "Léxico",
                    ex.getMessage(),
                    "-",
                    "-"
            });
        }
    }

    private void runDemo() {
        console.setText("");
        String input = editor.getText();

        // 1) Tokens + errores léxicos
        fillTokensAndLexErrors(input);

        // 2) Parse + capturar errores sintácticos
        DefaultTableModel errModel = (DefaultTableModel) errorsTable.getModel();
        final int[] nErr = { errModel.getRowCount() + 1 };

        try {
            analizadores.Lexico lex2 = new analizadores.Lexico(new StringReader(input));

            analizadores.Sintactico parser = new analizadores.Sintactico(
                    lex2,
                    msg -> console.append(msg + "\n"),
                    row -> { // row = {tipo, desc, lin, col}
                        errModel.addRow(new Object[]{
                                nErr[0]++,
                                row[0],
                                row[1],
                                row[2],
                                row[3]
                        });
                    }
            );

            parser.parse();

            console.append(">> Análisis completado: SIN errores.\n");
        } catch (Exception ex) {
            console.append(">> Error en análisis: " + ex.getMessage() + "\n");
        }
    }

    //representación del token
    private String tipoEnunciado(String tokenName) {
        return switch (tokenName) {

            case "ID" -> "id";
            case "ENTERO" -> "int";
            case "DECIMAL" -> "float";
            case "CADENA" -> "string";
            case "TRUE", "FALSE" -> "bool";
            case "NULL" -> "null";

            //clasificación útil
            case "DATABASE", "USE", "TABLE", "READ", "FIELDS", "FILTER", "STORE", "AT",
                 "EXPORT", "ADD", "UPDATE", "SET", "CLEAR" -> "keyword";

            case "IGUAL_IGUAL", "DIFERENTE", "MAYOR", "MENOR", "MAYOR_IGUAL", "MENOR_IGUAL" -> "relational_op";
            case "AND", "OR", "NOT" -> "logical_op";

            case "LLAVE_ABRE", "LLAVE_CIERRA", "PAR_ABRE", "PAR_CIERRA" -> "grouping_symbol";
            case "PUNTO_COMA", "COMA", "DOS_PUNTOS" -> "symbol";

            case "ERROR" -> "error";

            default -> tokenName;
        };
    }

    //reporte html
    private void generarHTMLReport() {
        try {
            DefaultTableModel tok = (DefaultTableModel) tokensTable.getModel();
            DefaultTableModel err = (DefaultTableModel) errorsTable.getModel();

            String html = buildHTML(tok, err);

            Path dir = Path.of("reportes");
            Files.createDirectories(dir);

            Path out = dir.resolve("reporte.html");
            Files.writeString(out, html, StandardCharsets.UTF_8);

            console.append(">> Reporte HTML generado: " + out.toAbsolutePath() + "\n");

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(out.toUri());
            }
        } catch (Exception ex) {
            console.append(">> No se pudo generar HTML: " + ex.getMessage() + "\n");
        }
    }

    private String buildHTML(DefaultTableModel tok, DefaultTableModel err) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!doctype html><html><head><meta charset='utf-8'>")
                .append("<title>Reporte ELI-NOSQL</title>")
                .append("<style>")
                .append("body{font-family:Arial;margin:20px;} h2{margin-top:30px;}")
                .append("table{border-collapse:collapse;width:100%;margin-top:10px;}")
                .append("th,td{border:1px solid #999;padding:8px;text-align:left;}")
                .append("th{background:#e9f2ff;}")
                .append("</style></head><body>");

        sb.append("<h1>Reporte ELI-NOSQL</h1>")
                .append("<p>Generado: ").append(LocalDateTime.now()).append("</p>");

        // TOKENS
        sb.append("<h2>Tabla de Tokens</h2>");
        sb.append("<table><tr>");
        for (int c = 0; c < tok.getColumnCount(); c++) sb.append("<th>").append(tok.getColumnName(c)).append("</th>");
        sb.append("</tr>");
        for (int r = 0; r < tok.getRowCount(); r++) {
            sb.append("<tr>");
            for (int c = 0; c < tok.getColumnCount(); c++) {
                Object v = tok.getValueAt(r, c);
                sb.append("<td>").append(v == null ? "" : escapeHtml(v.toString())).append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table>");

        // ERRORES
        sb.append("<h2>Tabla de Errores</h2>");
        sb.append("<table><tr>");
        for (int c = 0; c < err.getColumnCount(); c++) sb.append("<th>").append(err.getColumnName(c)).append("</th>");
        sb.append("</tr>");
        for (int r = 0; r < err.getRowCount(); r++) {
            sb.append("<tr>");
            for (int c = 0; c < err.getColumnCount(); c++) {
                Object v = err.getValueAt(r, c);
                sb.append("<td>").append(v == null ? "" : escapeHtml(v.toString())).append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table>");

        sb.append("</body></html>");
        return sb.toString();
    }

    private String escapeHtml(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&#39;");
    }
}
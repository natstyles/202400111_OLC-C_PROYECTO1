package com.eli.ide.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.Desktop;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import java_cup.runtime.Symbol;
import analizadores.sym;

public class IdeFrame extends JFrame {

    /* ====== File chooser ====== */
    private final JFileChooser fileChooser = new JFileChooser();
    private final FileNameExtensionFilter eliFilter =
            new FileNameExtensionFilter("Archivos ELI (*.eli)", "eli");

    /* ====== Multi-tab editor ====== */
    private final JTabbedPane editorTabs = new JTabbedPane();
    private final Map<Component, EditorTab> tabByComponent = new HashMap<>();
    private int untitledCounter = 1;

    /* ====== Consola / Output (globales) ====== */
    private final JTextArea console = new JTextArea();
    private final JTable tokensTable;
    private final JTable errorsTable;

    private static class EditorTab {
        final JTextArea area = new JTextArea();
        File file = null;
        boolean dirty = false;

        DocumentListener listener;
        Document attachedDocument;

        String displayName() {
            return (file == null) ? "Nuevo " : file.getName();
        }
    }

    public IdeFrame() {
        super("ELI-NOSQL");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (confirmCloseAllTabs()) dispose();
            }
        });

        setSize(1100, 700);
        setLocationRelativeTo(null);

        setJMenuBar(buildMenuBar());

        setupFileChooser();

        // ===== Editor Tabs =====
        editorTabs.setBorder(new TitledBorder("Editor"));
        editorTabs.addChangeListener(e -> updateTitle());

        // crea primera pestaña en blanco
        addNewTab(null, "");

        // ===== Consola =====
        console.setEditable(false);
        console.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JScrollPane consoleScroll = new JScrollPane(console);
        consoleScroll.setBorder(new TitledBorder("Consola"));

        // ===== Output (Tokens/Errores) =====
        tokensTable = new JTable(new DefaultTableModel(
                new Object[]{"#", "Lexema", "Tipo", "Línea", "Columna"}, 0
        ));
        JScrollPane tokensScroll = new JScrollPane(tokensTable);

        errorsTable = new JTable(new DefaultTableModel(
                new Object[]{"#", "Tipo", "Descripción", "Línea", "Columna"}, 0
        ));
        JScrollPane errorsScroll = new JScrollPane(errorsTable);

        JTabbedPane outputTabs = new JTabbedPane();
        outputTabs.addTab("Tokens", tokensScroll);
        outputTabs.addTab("Errores", errorsScroll);

        JScrollPane outputScroll = new JScrollPane(outputTabs);
        outputScroll.setBorder(new TitledBorder("Output"));

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, consoleScroll, outputScroll);
        rightSplit.setResizeWeight(0.55);

        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                editorTabs,
                rightSplit
        );
        mainSplit.setResizeWeight(0.6);

        add(mainSplit, BorderLayout.CENTER);

        updateTitle();
    }

    private void setupFileChooser() {
        fileChooser.resetChoosableFileFilters();
        fileChooser.setFileFilter(eliFilter);
        fileChooser.setAcceptAllFileFilterUsed(true);
    }

    private static boolean hasEliExtension(File f) {
        return f != null && f.getName().toLowerCase().endsWith(".eli");
    }

    private static File ensureEliExtension(File f) {
        if (f == null) return null;
        if (hasEliExtension(f)) return f;
        return new File(f.getParentFile(), f.getName() + ".eli");
    }

    /* ====== Menu ====== */
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

        JMenuItem cerrarPestana = new JMenuItem("Cerrar pestaña");
        cerrarPestana.addActionListener(e -> closeCurrentTab());

        file.add(nuevo);
        file.add(abrir);
        file.addSeparator();
        file.add(guardar);
        file.add(guardarComo);
        file.addSeparator();
        file.add(cerrarPestana);

        JMenu run = new JMenu("Ejecutar");
        JMenuItem ejecutar = new JMenuItem("Run");
        ejecutar.addActionListener(e -> runDemo());
        run.add(ejecutar);

        JMenu reportes = new JMenu("Reportes");
        JMenuItem html = new JMenuItem("Generar HTML");
        html.addActionListener(e -> generarHTMLReport());
        reportes.add(html);

        bar.add(file);
        bar.add(run);
        bar.add(reportes);

        return bar;
    }

    /* ====== Tabs helpers ====== */
    private EditorTab currentTab() {
        Component c = editorTabs.getSelectedComponent();
        return (c == null) ? null : tabByComponent.get(c);
    }

    private void addNewTab(File file, String content) {
        EditorTab tab = new EditorTab();
        tab.file = file;

        tab.area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        tab.area.setText(content);

        JScrollPane scroll = new JScrollPane(tab.area);

        // instalar tracking dirty por documento
        installDirtyTracking(tab);

        String title = tabTitle(tab);
        editorTabs.addTab(title, scroll);
        tabByComponent.put(scroll, tab);

        editorTabs.setSelectedComponent(scroll);
        updateTitle();
    }

    private String tabTitle(EditorTab tab) {
        String base;
        if (tab.file == null) {
            base = "Nuevo " + untitledCounter;
        } else {
            base = tab.file.getName();
        }
        return (tab.dirty ? "*" : "") + base;
    }

    private void refreshCurrentTabTitle() {
        EditorTab tab = currentTab();
        if (tab == null) return;

        Component c = editorTabs.getSelectedComponent();
        int idx = editorTabs.indexOfComponent(c);
        if (idx >= 0) editorTabs.setTitleAt(idx, tabTitle(tab));

        updateTitle();
    }

    private void installDirtyTracking(EditorTab tab) {
        if (tab.attachedDocument != null && tab.listener != null) {
            tab.attachedDocument.removeDocumentListener(tab.listener);
        }

        tab.listener = new DocumentListener() {
            private void changed() {
                if (!tab.dirty) {
                    tab.dirty = true;
                    refreshCurrentTabTitle();
                }
            }
            @Override public void insertUpdate(DocumentEvent e) { changed(); }
            @Override public void removeUpdate(DocumentEvent e) { changed(); }
            @Override public void changedUpdate(DocumentEvent e) { changed(); }
        };

        tab.attachedDocument = tab.area.getDocument();
        tab.attachedDocument.addDocumentListener(tab.listener);

        // al cargar texto inicial no queremos marcar dirty automáticamente
        tab.dirty = false;
    }

    private void updateTitle() {
        EditorTab tab = currentTab();
        if (tab == null) {
            setTitle("ELI NOSQL");
            return;
        }
        String name = (tab.file == null) ? "Nuevo" : tab.file.getName();
        setTitle("ELI NOSQL - " + (tab.dirty ? "*" : "") + name);
    }

    /* ====== Confirmaciones ====== */
    private boolean confirmSaveIfNeeded(EditorTab tab) {
        if (tab == null || !tab.dirty) return true;

        int option = JOptionPane.showConfirmDialog(
                this,
                "Tienes cambios sin guardar en \"" + tabTitle(tab).replace("*", "") + "\".\n¿Deseas guardarlos?",
                "Cambios sin guardar",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) return false;
        if (option == JOptionPane.YES_OPTION) return saveSpecificTab(tab);
        return true; // NO = descartar
    }

    private boolean confirmCloseAllTabs() {
        for (Component c : tabByComponent.keySet()) {
            EditorTab tab = tabByComponent.get(c);
            if (tab != null && tab.dirty) {
                // nos vamos una por una (si el usuario cancela, detenemos)
                editorTabs.setSelectedComponent(c);
                if (!confirmSaveIfNeeded(tab)) return false;
            }
        }
        return true;
    }

    /* ====== Acciones Archivo ====== */
    private void newFile() {
        addNewTab(null, "");
        untitledCounter++;
    }

    private void openFile() {
        fileChooser.setDialogTitle("Abrir archivo (.eli)");
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();

            if (!hasEliExtension(selected)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Solo se permiten archivos con extensión .eli",
                        "Archivo inválido",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // si ya está abierto, solo enfocar
            for (Map.Entry<Component, EditorTab> entry : tabByComponent.entrySet()) {
                EditorTab t = entry.getValue();
                if (t.file != null && t.file.equals(selected)) {
                    editorTabs.setSelectedComponent(entry.getKey());
                    return;
                }
            }

            try {
                String content = Files.readString(selected.toPath(), StandardCharsets.UTF_8);
                addNewTab(selected, content);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al abrir archivo: " + ex.getMessage());
            }
        }
    }

    private boolean saveFile() {
        EditorTab tab = currentTab();
        if (tab == null) return false;
        return saveSpecificTab(tab);
    }

    private boolean saveSpecificTab(EditorTab tab) {
        if (tab.file == null) return saveFileAs(tab);

        //Por si el archivo se asignó sin extensión (o el usuario tecleó sin .eli)
        tab.file = ensureEliExtension(tab.file);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tab.file), StandardCharsets.UTF_8
        ))) {
            tab.area.write(writer);
            tab.dirty = false;
            refreshCurrentTabTitle();
            return true;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar archivo: " + ex.getMessage());
            return false;
        }
    }

    private boolean saveFileAs() {
        EditorTab tab = currentTab();
        if (tab == null) return false;
        return saveFileAs(tab);
    }

    private boolean saveFileAs(EditorTab tab) {
        fileChooser.setDialogTitle("Guardar como (.eli)");
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File chosen = fileChooser.getSelectedFile();
            File target = ensureEliExtension(chosen);

            //Confirmar sobrescritura si existe
            if (target.exists()) {
                int opt = JOptionPane.showConfirmDialog(
                        this,
                        "El archivo ya existe:\n" + target.getName() + "\n¿Deseas sobrescribirlo?",
                        "Confirmar",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (opt != JOptionPane.YES_OPTION) return false;
            }

            tab.file = target;
            boolean ok = saveSpecificTab(tab);
            refreshCurrentTabTitle();
            return ok;
        }
        return false;
    }

    private void closeCurrentTab() {
        EditorTab tab = currentTab();
        if (tab == null) return;

        if (!confirmSaveIfNeeded(tab)) return;

        Component c = editorTabs.getSelectedComponent();
        tabByComponent.remove(c);
        editorTabs.remove(c);

        // si ya no quedan pestañas, crea una nueva
        if (editorTabs.getTabCount() == 0) {
            addNewTab(null, "");
            untitledCounter++;
        }

        updateTitle();
    }

    /* ====== Analisis ====== */
    private boolean fillTokensAndLexErrors(String input) {
        DefaultTableModel tok = (DefaultTableModel) tokensTable.getModel();
        DefaultTableModel err = (DefaultTableModel) errorsTable.getModel();
        tok.setRowCount(0);
        err.setRowCount(0);

        int nTok = 1;
        int nErr = 1;
        boolean hasLexError = false;

        try {
            analizadores.Lexico lex = new analizadores.Lexico(new StringReader(input));

            int guard = 0;
            final int MAX_TOKENS = 200000;

            while (true) {
                if (guard++ > MAX_TOKENS) {
                    hasLexError = true;
                    err.addRow(new Object[]{
                            nErr++,
                            "Léxico",
                            "Se excedió el límite de tokens (" + MAX_TOKENS + "). Posible bucle en el lexer.",
                            "-",
                            "-"
                    });
                    break;
                }

                Symbol s = lex.next_token();
                if (s == null) break;
                if (s.sym == sym.EOF) break;

                String tokenName = (s.sym >= 0 && s.sym < sym.terminalNames.length)
                        ? sym.terminalNames[s.sym]
                        : ("SYM_" + s.sym);

                String lexema = (s.value == null) ? "" : s.value.toString();
                int linea = s.left;
                int columna = s.right;

                if ("ERROR".equals(tokenName)) {
                    hasLexError = true;
                    err.addRow(new Object[]{ nErr++, "Léxico", lexema, linea, columna });
                    continue;
                }

                tok.addRow(new Object[]{ nTok++, lexema, tipoEnunciado(tokenName), linea, columna });
            }
        } catch (Exception ex) {
            hasLexError = true;
            err.addRow(new Object[]{ nErr++, "Léxico", ex.getMessage(), "-", "-" });
        }

        return hasLexError;
    }

    private void runDemo() {
        console.setText("");

        EditorTab tab = currentTab();
        if (tab == null) return;

        String input = tab.area.getText();

        // 1) Tokens + errores léxicos
        boolean hasLexError = fillTokensAndLexErrors(input);
        if (hasLexError) {
            console.append(">> Se encontraron errores léxicos. No se ejecutó el análisis sintáctico.\n");
            return;
        }

        // 2) Parse + capturar errores sintácticos
        javax.swing.table.DefaultTableModel errModel = (javax.swing.table.DefaultTableModel) errorsTable.getModel();
        final int[] nErr = { errModel.getRowCount() + 1 };

        try {
            analizadores.Lexico lex2 = new analizadores.Lexico(new java.io.StringReader(input));

            analizadores.Sintactico parser = new analizadores.Sintactico(
                    lex2,
                    msg -> console.append(msg + "\n"),
                    row -> {
                        errModel.addRow(new Object[]{
                                nErr[0]++,
                                row[0],     // Tipo
                                row[1],     // Descripción
                                row[2],     // Línea
                                row[3]      // Columna
                        });
                    }
            );

            // 3) Ejecutamos el parser y capturamos la raíz del AST
            java_cup.runtime.Symbol symRaiz = parser.parse();

            // 4) Comprobamos la bandera de errores que configuramos en CUP
            if (parser.hayErrores) {
                console.append(">> Análisis finalizado. Se encontraron ERRORES SINTÁCTICOS. Revisa la tabla de errores.\n");
            } else {
                console.append(">> Análisis completado: SIN errores.\n");

                // INICIO DE LA EJECUCIÓN DEL ÁRBOL AST
                console.append("\n>> --- INICIANDO EJECUCIÓN --- \n");

                // Verificamos que el árbol no venga vacío
                if (symRaiz != null && symRaiz.value != null) {

                    //memoria global y conexión a UI
                    ejecucion.Entorno entornoGlobal = new ejecucion.Entorno(null, msg -> console.append(msg + "\n"));

                    // Extraemos la lista de instrucciones de la raíz
                    @SuppressWarnings("unchecked")
                    java.util.LinkedList<ejecucion.Instruccion> ast =
                            (java.util.LinkedList<ejecucion.Instruccion>) symRaiz.value;

                    // Recorremos y ejecutamos cada instrucción
                    for (ejecucion.Instruccion inst : ast) {
                        if (inst != null) {
                            // Al llamar a ejecutar, la instrucción hace su trabajo en memoria
                            // Puedes modificar luego la interfaz para que devuelva un String y pintarlo aquí
                            inst.ejecutar(entornoGlobal);
                        }
                    }

                    // Si la instrucción imprimió en consola (System.out), lo ideal más adelante
                    // es pasarle la consola de la UI al entorno para imprimirlo directo ahí.
                    console.append(">> --- EJECUCIÓN FINALIZADA --- \n");

                } else {
                    console.append(">> El código no contiene instrucciones ejecutables.\n");
                }
            }

        } catch (Exception ex) {
            console.append(">> Error fatal en el análisis: No se pudo continuar compilando.\n");
            ex.printStackTrace(); // Muy útil para depurar si falla el casteo del AST
        }
    }

    private String tipoEnunciado(String tokenName) {
        return switch (tokenName) {
            case "ID" -> "id";
            case "ENTERO" -> "int";
            case "DECIMAL" -> "float";
            case "CADENA" -> "string";
            case "TRUE", "FALSE" -> "bool";
            case "NULL" -> "null";

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

    /* ====== Reporte HTML ====== */
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

        sb.append("<h2>Tabla de Tokens</h2><table><tr>");
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

        sb.append("<h2>Tabla de Errores</h2><table><tr>");
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
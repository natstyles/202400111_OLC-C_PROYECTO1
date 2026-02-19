package analizadores;

import java_cup.runtime.Symbol;

%%

%class Lexico
%public
%unicode
%cup
%line
%column

/* Estado exclusivo para comentarios multilínea */
%xstate MLCOMMENT

%{
  private Symbol sym(int type) {
    return new Symbol(type, yyline + 1, yycolumn + 1, yytext());
  }

  private Symbol sym(int type, Object value) {
    return new Symbol(type, yyline + 1, yycolumn + 1, value);
  }
%}

/* ===== Macros ===== */
WS       = [ \t\r\n\f]+
ID       = [a-zA-Z_][a-zA-Z0-9_]*
INT      = -?[0-9]+
DEC      = -?[0-9]+"."[0-9]+
STRING   = \"([^\"\\\r\n]|\\.)*\"

/* Comentario de línea: ## .... */
LINECMT  = "##".*

%%

/* ===== Ignorar ===== */
{WS}        { /* ignore */ }
{LINECMT}   { /* ignore */ }

/* ===== Comentario multilínea: #* .... #* ===== */
<YYINITIAL> "#*" {
    // System.out.println("DEBUG: Inicio de comentario multilinea");
    yybegin(MLCOMMENT);
}

<MLCOMMENT> {
    /* Cierre del comentario */
    "#*" {
        // System.out.println("DEBUG: Fin de comentario multilinea");
        yybegin(YYINITIAL);
    }

    /* Si llega EOF sin cerrar, reporta 1 error y luego permite terminar en EOF normal */
    <<EOF>> {
        yybegin(YYINITIAL);
        return sym(sym.ERROR, "Comentario multilínea sin cerrar al final del archivo");
    }

    /* Consumir absolutamente cualquier carácter (incluye \n, \r, etc.) */
    [^] { /* ignore */ }
}

/* ===== Palabras reservadas ===== */
"database"   { return sym(sym.DATABASE); }
"use"        { return sym(sym.USE); }
"table"      { return sym(sym.TABLE); }
"read"       { return sym(sym.READ); }
"fields"     { return sym(sym.FIELDS); }
"filter"     { return sym(sym.FILTER); }
"store"      { return sym(sym.STORE); }
"at"         { return sym(sym.AT); }
"export"     { return sym(sym.EXPORT); }
"add"        { return sym(sym.ADD); }
"update"     { return sym(sym.UPDATE); }
"set"        { return sym(sym.SET); }
"clear"      { return sym(sym.CLEAR); }

/* ===== Tipos ===== */
"int"        { return sym(sym.T_INT); }
"string"     { return sym(sym.T_STRING); }
"float"      { return sym(sym.T_FLOAT); }
"bool"       { return sym(sym.T_BOOL); }

/* ===== Literales boolean/null ===== */
"true"       { return sym(sym.TRUE); }
"false"      { return sym(sym.FALSE); }
"null"       { return sym(sym.NULL); }

/* ===== Símbolos ===== */
"{"          { return sym(sym.LLAVE_ABRE); }
"}"          { return sym(sym.LLAVE_CIERRA); }
"("          { return sym(sym.PAR_ABRE); }
")"          { return sym(sym.PAR_CIERRA); }
"["          { return sym(sym.COR_ABRE); }
"]"          { return sym(sym.COR_CIERRA); }

":"          { return sym(sym.DOS_PUNTOS); }
";"          { return sym(sym.PUNTO_COMA); }
","          { return sym(sym.COMA); }
"*"          { return sym(sym.ASTERISCO); }

/* ===== Operadores ===== */
">="         { return sym(sym.MAYOR_IGUAL); }
"<="         { return sym(sym.MENOR_IGUAL); }
"=="         { return sym(sym.IGUAL_IGUAL); }
"!="         { return sym(sym.DIFERENTE); }
">"          { return sym(sym.MAYOR); }
"<"          { return sym(sym.MENOR); }

"&&"         { return sym(sym.AND); }
"||"         { return sym(sym.OR); }
"!"          { return sym(sym.NOT); }

"="          { return sym(sym.IGUAL); }

/* ===== Literales ===== */
{DEC}        { return sym(sym.DECIMAL, yytext()); }
{INT}        { return sym(sym.ENTERO, yytext()); }
{STRING}     {
              String s = yytext();
              return sym(sym.CADENA, s.substring(1, s.length()-1));
             }

/* ===== Identificadores ===== */
{ID}         { return sym(sym.ID, yytext()); }

/* ===== Error léxico ===== */
.            { return sym(sym.ERROR, "Carácter inválido: " + yytext()); }

<<EOF>>      { return new Symbol(sym.EOF); }

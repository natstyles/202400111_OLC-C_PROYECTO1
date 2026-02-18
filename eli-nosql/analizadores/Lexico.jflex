package analizadores;

import java_cup.runtime.Symbol;

%%

%class Lexico
%public
%unicode
%cup
%line
%column

%{
  private Symbol sym(int type) {
    return new Symbol(type, yyline + 1, yycolumn + 1, yytext());
  }

  private Symbol sym(int type, Object value) {
    return new Symbol(type, yyline + 1, yycolumn + 1, value);
  }
%}

/* ===== Macros ===== */
WS      = [ \t\r\n\f]+
ID      = [a-zA-Z_][a-zA-Z0-9_]*
INT     = [0-9]+
DEC     = [0-9]+"."[0-9]+
STRING = \"([^\"\\\r\n]|\\.)*\"

/* comentarios de línea (// ...) */
LINECMT = "//".*

%%

/* ===== Ignorar ===== */
{WS}        { /* ignore */ }
{LINECMT}   { /* ignore */ }

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

/* tipos */
"int"        { return sym(sym.T_INT); }
"string"     { return sym(sym.T_STRING); }

/* booleanos */
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
/* Relacionales (poner primero los de 2 chars) */
">="         { return sym(sym.MAYOR_IGUAL); }
"<="         { return sym(sym.MENOR_IGUAL); }
"=="         { return sym(sym.IGUAL_IGUAL); }
"!="         { return sym(sym.DIFERENTE); }
">"          { return sym(sym.MAYOR); }
"<"          { return sym(sym.MENOR); }

/* Lógicos */
"&&"         { return sym(sym.AND); }
"||"         { return sym(sym.OR); }
"!"          { return sym(sym.NOT); }

/* Asignación */
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

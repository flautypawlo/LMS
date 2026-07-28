package util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonUtil {

    private JsonUtil() {
    }

    // ---------- SERIALIZACIÓN ----------

    public static String listaDeMapasAJson(List<Map<String, Object>> lista) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < lista.size(); i++) {
            sb.append("  ").append(mapaAJson(lista.get(i)));
            if (i < lista.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static String valorAJson(Object valor) {
        if (valor == null) {
            return "null";
        }
        if (valor instanceof String) {
            return "\"" + escapar((String) valor) + "\"";
        }
        if (valor instanceof Number || valor instanceof Boolean) {
            return String.valueOf(valor);
        }
        if (valor instanceof Map) {
            return mapaAJson((Map<String, Object>) valor);
        }
        if (valor instanceof List) {
            return listaAJson((List<Object>) valor);
        }
        return "\"" + escapar(String.valueOf(valor)) + "\"";
    }

    private static String mapaAJson(Map<String, Object> mapa) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int indice = 0;
        int total = mapa.size();
        for (Map.Entry<String, Object> entrada : mapa.entrySet()) {
            sb.append("\"").append(escapar(entrada.getKey())).append("\":");
            sb.append(valorAJson(entrada.getValue()));
            if (indice < total - 1) {
                sb.append(",");
            }
            indice++;
        }
        sb.append("}");
        return sb.toString();
    }

    private static String listaAJson(List<Object> lista) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(valorAJson(lista.get(i)));
            if (i < lista.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escapar(String texto) {
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    // ---------- PARSEO ----------

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> jsonAListaDeMapas(String json) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) {
            return resultado;
        }
        AnalizadorJson analizador = new AnalizadorJson(json);
        Object valor = analizador.analizarValor();
        if (valor instanceof List) {
            for (Object elemento : (List<Object>) valor) {
                if (elemento instanceof Map) {
                    resultado.add((Map<String, Object>) elemento);
                }
            }
        }
        return resultado;
    }

    // ---------- HELPERS DE LECTURA SEGURA ----------

    public static String getString(Map<String, Object> mapa, String clave) {
        Object valor = mapa.get(clave);
        return valor == null ? null : String.valueOf(valor);
    }

    public static int getInt(Map<String, Object> mapa, String clave) {
        Object valor = mapa.get(clave);
        if (valor == null) {
            return 0;
        }
        if (valor instanceof Number) {
            return ((Number) valor).intValue();
        }
        return Integer.parseInt(String.valueOf(valor));
    }

    public static double getDouble(Map<String, Object> mapa, String clave) {
        Object valor = mapa.get(clave);
        if (valor == null) {
            return 0.0;
        }
        if (valor instanceof Number) {
            return ((Number) valor).doubleValue();
        }
        return Double.parseDouble(String.valueOf(valor));
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getLista(Map<String, Object> mapa, String clave) {
        Object valor = mapa.get(clave);
        if (valor instanceof List) {
            return (List<Object>) valor;
        }
        return new ArrayList<>();
    }

    // ---------- ANALIZADOR INTERNO ----------

    private static class AnalizadorJson {

        private final String texto;
        private int posicion;

        AnalizadorJson(String texto) {
            this.texto = texto;
            this.posicion = 0;
        }

        Object analizarValor() {
            saltarEspacios();
            char actual = texto.charAt(posicion);
            if (actual == '{') {
                return analizarObjeto();
            }
            if (actual == '[') {
                return analizarArreglo();
            }
            if (actual == '"') {
                return analizarCadena();
            }
            if (actual == 't' || actual == 'f') {
                return analizarBooleano();
            }
            if (actual == 'n') {
                posicion += 4;
                return null;
            }
            return analizarNumero();
        }

        private Map<String, Object> analizarObjeto() {
            Map<String, Object> mapa = new LinkedHashMap<>();
            posicion++;
            saltarEspacios();
            if (texto.charAt(posicion) == '}') {
                posicion++;
                return mapa;
            }
            while (true) {
                saltarEspacios();
                String clave = analizarCadena();
                saltarEspacios();
                posicion++;
                Object valor = analizarValor();
                mapa.put(clave, valor);
                saltarEspacios();
                char siguiente = texto.charAt(posicion);
                if (siguiente == ',') {
                    posicion++;
                    continue;
                }
                if (siguiente == '}') {
                    posicion++;
                    break;
                }
            }
            return mapa;
        }

        private List<Object> analizarArreglo() {
            List<Object> lista = new ArrayList<>();
            posicion++;
            saltarEspacios();
            if (texto.charAt(posicion) == ']') {
                posicion++;
                return lista;
            }
            while (true) {
                Object valor = analizarValor();
                lista.add(valor);
                saltarEspacios();
                char siguiente = texto.charAt(posicion);
                if (siguiente == ',') {
                    posicion++;
                    continue;
                }
                if (siguiente == ']') {
                    posicion++;
                    break;
                }
            }
            return lista;
        }

        private String analizarCadena() {
            StringBuilder sb = new StringBuilder();
            posicion++;
            while (texto.charAt(posicion) != '"') {
                char actual = texto.charAt(posicion);
                if (actual == '\\') {
                    posicion++;
                    char siguiente = texto.charAt(posicion);
                    switch (siguiente) {
                        case '"':
                            sb.append('"');
                            break;
                        case '\\':
                            sb.append('\\');
                            break;
                        case '/':
                            sb.append('/');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'u':
                            String hex = texto.substring(posicion + 1, posicion + 5);
                            sb.append((char) Integer.parseInt(hex, 16));
                            posicion += 4;
                            break;
                        default:
                            sb.append(siguiente);
                    }
                } else {
                    sb.append(actual);
                }
                posicion++;
            }
            posicion++;
            return sb.toString();
        }

        private Boolean analizarBooleano() {
            if (texto.charAt(posicion) == 't') {
                posicion += 4;
                return Boolean.TRUE;
            }
            posicion += 5;
            return Boolean.FALSE;
        }

        private Object analizarNumero() {
            int inicio = posicion;
            boolean esDecimal = false;
            while (posicion < texto.length()) {
                char actual = texto.charAt(posicion);
                if (Character.isDigit(actual) || actual == '-' || actual == '+') {
                    posicion++;
                } else if (actual == '.' || actual == 'e' || actual == 'E') {
                    esDecimal = true;
                    posicion++;
                } else {
                    break;
                }
            }
            String numeroTexto = texto.substring(inicio, posicion);
            if (esDecimal) {
                return Double.parseDouble(numeroTexto);
            }
            return Integer.parseInt(numeroTexto);
        }

        private void saltarEspacios() {
            while (posicion < texto.length() && Character.isWhitespace(texto.charAt(posicion))) {
                posicion++;
            }
        }
    }
}
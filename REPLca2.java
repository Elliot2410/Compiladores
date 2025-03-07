import java.io.*;
import java.util.Scanner; // java.util.Scanner para lectura de archivos o entrada estándar
import java.util.List;

public class REPLca2 {
    public static void main(String[] args) {
        String source = "";
        if (args.length > 1) {
            System.out.println("Se recibieron más argumentos");
            System.exit(1);
        }
        if (args.length == 1) {
            String argumento = args[0];
            try {
                Scanner fileScanner = new Scanner(new File(argumento));
                StringBuilder sb = new StringBuilder();
                while (fileScanner.hasNextLine()) {
                    sb.append(fileScanner.nextLine());
                    sb.append("\n");
                }
                fileScanner.close();
                source = sb.toString();
            } catch (FileNotFoundException e) {
                System.out.println("Error al abrir el archivo.");
                System.exit(1);
            }
        } else {
            // Leer desde entrada estándar
            Scanner inputScanner = new Scanner(System.in);
            StringBuilder sb = new StringBuilder();
            while (inputScanner.hasNextLine()) {
                sb.append(inputScanner.nextLine());
                sb.append("\n");
            }
            inputScanner.close();
            source = sb.toString();
        }

        // Ejecutar el Lexer sobre el código fuente
        Lexer lexer = new Lexer(source);
        List<Token> tokens;
        try {
            tokens = lexer.scanTokens();
            boolean errorEncontrado = false;

            for (Token token : tokens) {
                if(token.getTipo() == TipoToken.ERROR) {
                    errorEncontrado = true;
                    break;
                }

                // Imprime el token según su tipo
                if (token.getTipo() == TipoToken.IDENTIFIER) {
                    System.out.println("<ID, lexema: " + token.getLexema() + ", línea: " + token.getLine() + ">");
                } else if (token.getTipo() == TipoToken.VAR) {
                    System.out.println("<VAR, línea: " + token.getLine() + ">");
                } else if (token.getTipo() == TipoToken.EQUAL) {
                    System.out.println("<EQUAL, línea: " + token.getLine() + ">");
                } else {
                    System.out.println(token);
                }
            }

            if (errorEncontrado) {
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("Error durante el análisis léxico: " + e.getMessage());
            System.exit(1);
        }
    }
}

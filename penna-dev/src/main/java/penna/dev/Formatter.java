package penna.dev;

public class Formatter {

    public static String format(String message, Object[] args) {
        StringBuilder formattedMessage = new StringBuilder(message);
        int cursor = 0;
        int pos;
        int arg = 0;

        while ((pos = formattedMessage.indexOf("{}", cursor)) >= 0 && arg < args.length) {
            if (pos > 0 && formattedMessage.codePointAt(pos - 1) == '\\' &&
                    (pos - 1 == 0 || formattedMessage.codePointBefore(pos - 1) != '\\')) {
                cursor = pos + 1;
            } else {
                String formattedArg;
                if (args[arg] == null) {
                    formattedArg = "null";
                } else {
                    formattedArg = args[arg].toString();
                }
                arg++;
                formattedMessage.replace(pos, pos + 2, formattedArg);
            }
        }

        return formattedMessage.toString();
    }
}

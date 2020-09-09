package net.ziyoung.shadow4j.server;

import org.apache.commons.cli.*;

public class Main {

    private static final Options options = new Options();

    public static void main(String[] args) throws Exception {
        CommandLine commandLine = commandLine(args);
        HelpFormatter helpFormatter = new HelpFormatter();

        if (commandLine.hasOption("h")) {
            helpFormatter.printHelp("shadow-server", options);
            return;
        }

        new Server(null).start();
    }

    private static CommandLine commandLine(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();

        options.addOption("h", false, "help information");
        options.addOption("verbose", false, "verbose mode");

        Option c = Option.builder("s")
                .longOpt("server-url")
                .desc("server config url")
                .hasArg().build();
        options.addOption(c);

        return parser.parse(options, args);
    }


}

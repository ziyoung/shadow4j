package net.ziyoung.shadow4j.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

@Slf4j
public class Main {

    public static void main(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();

        options.addOption("verbose", false, "verbose mode");
        Option socks = Option.builder("s")
                .longOpt("socks")
                .desc("SOCKS listen address")
                .hasArg().build();
        options.addOption(socks);
        Option c = Option.builder("c")
                .longOpt("client-url")
                .desc("client connect address or url")
                .hasArg().build();
        options.addOption(c);
        options.addOption("h", false, "help information");

        CommandLine commandLine = parser.parse(options, args);

        if (commandLine.hasOption("h")) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("shadow-client", options);
            return;
        }

        if (commandLine.hasOption("verbose")) {
            log.info("verbose");
        }
    }

}

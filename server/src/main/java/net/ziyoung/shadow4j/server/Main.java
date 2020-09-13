package net.ziyoung.shadow4j.server;

import lombok.extern.slf4j.Slf4j;
import net.ziyoung.shadow4j.shadow.ShadowConfig;
import net.ziyoung.shadow4j.shadow.ShadowUtil;
import org.apache.commons.cli.*;

@Slf4j
public class Main {

    private static final Options options = new Options();

    public static void main(String[] args) throws Exception {
        CommandLine commandLine = commandLine(args);
        HelpFormatter helpFormatter = new HelpFormatter();

        if (commandLine.hasOption("h")) {
            helpFormatter.printHelp("shadow-server", options);
            return;
        }

        ServerConfig config = readServerConfig(commandLine);
        log.info("server config: {} ", config);

        new Server(config).start();
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


    private static ServerConfig readServerConfig(CommandLine commandLine) throws Exception {
        boolean verboseMode = commandLine.hasOption("verbose");
        String url = commandLine.getOptionValue("s").trim();
        ShadowConfig shadowConfig = ShadowUtil.parseClientUrl(url);
        int port = shadowConfig.getServer().getPort();

        return ServerConfig.builder()
                .shadowConfig(shadowConfig)
                .verboseMode(verboseMode)
                .port(port)
                .build();
    }
}

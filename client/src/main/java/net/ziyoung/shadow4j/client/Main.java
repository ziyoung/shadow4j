package net.ziyoung.shadow4j.client;

import lombok.extern.slf4j.Slf4j;
import net.ziyoung.shadow4j.shadow.ShadowConfig;
import net.ziyoung.shadow4j.shadow.ShadowUtil;
import org.apache.commons.cli.*;

import java.net.InetSocketAddress;

@Slf4j
public class Main {

    private static final Options options = new Options();

    public static void main(String[] args) throws Exception {
        CommandLine commandLine = commandLine(args);
        HelpFormatter helpFormatter = new HelpFormatter();

        boolean displayHelp = commandLine.hasOption("h") || !(commandLine.hasOption("c") && commandLine.hasOption("s"));
        if (displayHelp) {
            helpFormatter.printHelp("shadow-client", options);
            return;
        }

        ClientConfig clientConfig = readClientConfig(commandLine);
        log.info("client clientConfig: {} ", clientConfig);

        new Client(clientConfig).start();
    }

    private static CommandLine commandLine(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();

        options.addOption("h", false, "help information");
        options.addOption("verbose", false, "verbose mode");

        Option c = Option.builder("c")
                .longOpt("client-url")
                .desc("client connect address or url")
                .hasArg().build();
        options.addOption(c);

        Option socks = Option.builder("s")
                .longOpt("socks")
                .desc("listen address")
                .hasArg().build();
        options.addOption(socks);

//        Option u = Option.builder("u")
//                .longOpt("udp-socks")
//                .desc("Enable UDP support for SOCKS")
//                .build();
//        options.addOption(u);

        return parser.parse(options, args);
    }

    private static ClientConfig readClientConfig(CommandLine commandLine) throws Exception {
        boolean verboseMode = commandLine.hasOption("verbose");
        String url = commandLine.getOptionValue("c").trim();
        ShadowConfig shadowConfig = ShadowUtil.parseClientUrl(url);
        int port = ShadowUtil.parseSocksOption(commandLine.getOptionValue("s").trim());

        return ClientConfig.builder()
                .verboseMode(verboseMode)
                .shadowConfig(shadowConfig)
                .socks(new InetSocketAddress("127.0.0.1", port)).build();
    }

}

package net.ziyoung.shadow4j.client;

import lombok.extern.slf4j.Slf4j;
import net.ziyoung.shadow4j.shadow.KdUtil;
import net.ziyoung.shadow4j.shadow.MetaCipher;
import net.ziyoung.shadow4j.shadow.ShadowConfig;
import net.ziyoung.shadow4j.shadow.ShadowUtils;
import org.apache.commons.cli.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

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
        log.info("client clientConfig: {} ", clientConfig.toString());

        new Client(clientConfig).start();
    }

    private static CommandLine commandLine(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();

        options.addOption("verbose", false, "verbose mode");
        options.addOption("h", false, "help information");
        options.addOption("s", true, "listen address");

        Option c = Option.builder("c")
                .longOpt("client-url")
                .desc("client connect address or url")
                .hasArg().build();
        options.addOption(c);

        Option u = Option.builder("u")
                .longOpt("udp-socks")
                .desc("Enable UDP support for SOCKS")
                .build();
        options.addOption(u);

        return parser.parse(options, args);
    }

    private static ClientConfig readClientConfig(CommandLine commandLine) throws Exception {
        boolean verboseMode = commandLine.hasOption("verbose");
        String url = commandLine.getOptionValue("c").trim();
        ShadowConfig shadowConfig = parseClientUrl(url);
        int port = ShadowUtils.parseSocksOption(commandLine.getOptionValue("s").trim());

        return ClientConfig.builder()
                .verboseMode(verboseMode)
                .shadowConfig(shadowConfig)
                .socks(new InetSocketAddress("127.0.0.1", port)).build();
    }

    private static ShadowConfig parseClientUrl(String url) throws Exception {
        if (!url.startsWith("ss://")) {
            url = "ss://" + url;
        }
        URI uri = new URI(url);
        String userInfo = uri.getUserInfo();
        String host = uri.getHost();
        int port = uri.getPort();
        if (userInfo == null || host == null || port == -1) {
            throw new IllegalArgumentException("invalid url");
        }

        String[] strings = userInfo.split(":");
        if (strings.length != 2) {
            throw new IllegalArgumentException("invalid authority: cipher and password is required");
        }

        String cipher = strings[0].toLowerCase();
        byte[] password = strings[1].getBytes(StandardCharsets.UTF_8);
        if (!MetaCipher.CIPHER_CONFIG.containsKey(cipher)) {
            throw new IllegalArgumentException("invalid cipher name");
        }
        if (password.length == 0) {
            throw new IllegalArgumentException("password is required");
        }
        int size = MetaCipher.CIPHER_CONFIG.get(cipher);
        byte[] key = KdUtil.computeKdf(password, size);

        return new ShadowConfig(new InetSocketAddress(host, port), strings[0], key);
    }

}

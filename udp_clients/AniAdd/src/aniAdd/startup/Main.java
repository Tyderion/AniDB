package aniAdd.startup;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import aniAdd.Modules.IModule;
import aniAdd.*;
import aniAdd.Communication.ComEvent;
import gui.GUI;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.commons.cli.*;
import udpApi.Mod_UdpApi;

/**
 * @author Arokh
 */
public class Main {

    static String username, session, password, autopass;
    static JFrame frm = new JFrame();
    static AniAdd aniAdd;

    private static class AOMOptions {
        public static AOMOption username = new AOMOption("u", "username", "username", true);
        public static AOMOption password = new AOMOption("p", "password", "password", true);
        public static AOMOption noGui = new AOMOption(null, "no-gui", "Use cli instead of GUI.", false);
        public static AOMOption help = new AOMOption("h", "help", "print this help message", false);
        public static AOMOption config = new AOMOption("c", "config", "the path to the config file. Specified parameters will override values from the config file.", false);

        public static Options toOptions() {
            Options options = new Options();
            options.addOption(AOMOptions.username.toOption());
            options.addOption(AOMOptions.password.toOption());
            options.addOption(AOMOptions.noGui.toOption());
            options.addOption(AOMOptions.help.toOption());
            options.addOption(AOMOptions.config.toOption());
            return options;
        }
    }

    private static Options sOptions = AOMOptions.toOptions();

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception ex) {
        }
        /*try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ex) { }*/

        aniAdd = new AniAdd();

        // create Options object
        CommandLineParser parser = new DefaultParser();
        boolean printHelp = false;
        try {
            CommandLine cmd = parser.parse(sOptions, args);
            printHelp = cmd.hasOption(AOMOptions.help.getName());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (printHelp) {
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("aom", sOptions);
            System.exit(0);
        }
        System.exit(0);

        frm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frm.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                aniAdd.Stop();
            }
        });

        aniAdd.addComListener(new Communication.ComListener() {

            public void EventHandler(ComEvent comEvent) {
                if (comEvent.Type() == ComEvent.eType.Information) {
                    if ((IModule.eModState) comEvent.Params(0) == IModule.eModState.Initialized) {
                        Initialize();
                    }
                }
            }
        });

        aniAdd.Start();
    }

    private static void Initialize() {
        GUI gui = (GUI) aniAdd.GetModule("MainGUI");
        Mod_UdpApi api = (Mod_UdpApi) aniAdd.GetModule("UdpApi");

        username = JOptionPane.showInputDialog(frm, "User", "");
        password = JOptionPane.showInputDialog(frm, "Password", "");
        api.setPassword(password);
        api.setAniDBSession(session);
        api.setUsername(username);

        if (api.authenticate()) {
        } else {
        }

        frm.setDefaultLookAndFeelDecorated(true);

        frm.add(gui);
        frm.pack();
        frm.setVisible(true);
    }
}

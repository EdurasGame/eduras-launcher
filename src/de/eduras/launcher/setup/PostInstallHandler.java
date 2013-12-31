package de.eduras.launcher.setup;

import java.io.IOException;

import javax.swing.JOptionPane;

import jwrapper.jwutils.JWInstallApp;

public class PostInstallHandler {

	public static void main(String[] args) {
		try {
			// TODO add release version to shortcut name
			JWInstallApp.addAppShortcut("Test Shortcut", "Eduras");
			JWInstallApp.addUninstallerShortcut("Uninstall Eduras");
			System.out.println("it worked postinstall");
			JOptionPane.showMessageDialog(null, "it works!");

			// continue with jwrapper setup but skip the standard shortcut
			// installation
			JWInstallApp.exitJvm_ContinueAndSkipStandardSetup();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

import { commands, window, workspace, ExtensionContext } from 'vscode';
import * as path from 'path';
import * as requirements from './requirements/requirements';
import { retrieveJavaExecutable } from './requirements/JavaManager';

import {
	Executable,
	LanguageClient,
	LanguageClientOptions
} from 'vscode-languageclient/node';

let client: LanguageClient;

export async function activate(context: ExtensionContext) {

	const serverPath = context.asAbsolutePath(path.join('jars','server.jar'));
    console.log(serverPath);

    const requirementsData = await computeRequirementsData(context);

    const serverOptions: Executable = {
      command: retrieveJavaExecutable(requirementsData),
      args: [ '-jar', serverPath]
    };

	const clientOptions: LanguageClientOptions = {
		documentSelector: [{ scheme: 'file', language: 'redmatch' }]
	};

	// Create the language client and start the client.
	client = new LanguageClient(
		'RedmatchLanguageClient',
		'Redmatch Language Client',
		serverOptions,
		clientOptions
	);

	// Start the client. This will also launch the server
	client.start();
}

export function deactivate(): Thenable<void> | undefined {
	if (!client) {
		return undefined;
	}
	return client.stop();
}

async function computeRequirementsData(context: ExtensionContext) {
	try {
		return await requirements.resolveRequirements(context);
	} catch (error) {
		// show error
		const selection = await window.showErrorMessage(error.message, error.label);
		if (error.label && error.label === selection && error.command) {
			commands.executeCommand(error.command, error.commandParam);
		}
		// rethrow to disrupt the chain.
		throw error;
	}
}

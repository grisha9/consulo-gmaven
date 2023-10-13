
open module consulo.gmaven
{
	requires consulo.gmaven.server.api;
	requires consulo.maven.server;

	requires consulo.ide.api;
	requires consulo.util.nodep;

	requires com.intellij.xml;
	requires com.intellij.properties;

	requires consulo.java.execution.api;
	requires consulo.java.execution.impl;
	requires consulo.java.compiler.artifact.impl;
	requires consulo.java.language.api;
	requires consulo.java;

	requires java.rmi;

	requires jakarta.xml.bind;

	// TODO remove in future
	requires consulo.ide.impl;
	requires java.desktop;
	requires forms.rt;
}
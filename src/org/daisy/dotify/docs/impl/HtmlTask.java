package org.daisy.dotify.docs.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.BaseFolder;
import org.daisy.streamline.api.media.DefaultAnnotatedFile;
import org.daisy.streamline.api.media.DefaultFileSet;
import org.daisy.streamline.api.media.FileSet;
import org.daisy.streamline.api.media.ModifiableFileSet;
import org.daisy.streamline.api.option.UserOption;
import org.daisy.streamline.api.tasks.InternalTaskException;
import org.daisy.streamline.api.tasks.ReadWriteTask;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.select.Elements;

public class HtmlTask extends ReadWriteTask {
	private static final Logger LOGGER = Logger.getLogger(HtmlTask.class.getCanonicalName());
	private static final String SOURCE_LANGUAGE = "source-language";
	private static final String DEFAULT_LANGUAGE = Locale.getDefault().toLanguageTag();
	private static final String UTF_8 = "utf-8";
	private static List<UserOption> options = null;
	private final Optional<String> language;
	private final String outputCharset;
	
	public HtmlTask(Map<String, Object> params) {
		super("HTML to XHTML");
		this.language = getLanguage(params);
		this.outputCharset = UTF_8;
	}

	private static Optional<String> getLanguage(Map<String, Object> params) {
		Object param = params.get(SOURCE_LANGUAGE);
		return (param!=null)?Optional.of(""+param):Optional.empty();
	}

	@Override
	@Deprecated
	public void execute(File input, File output) throws InternalTaskException {
		execute(new DefaultAnnotatedFile.Builder(input).build(), output);
	}

	@Override
	public AnnotatedFile execute(AnnotatedFile input, File output) throws InternalTaskException {
		try {
			org.jsoup.nodes.Document doc = cleanup(parse(input));
			Files.write(output.toPath(), doc.html().getBytes(outputCharset));
			return asXHTML(output.toPath());
		} catch (IOException e) {
			throw new InternalTaskException(e);
		}
	}

	@Override
	public ModifiableFileSet execute(FileSet input, BaseFolder output) throws InternalTaskException {
		Path iManifestFolderPath = input.getBaseFolder().getPath().relativize(input.getManifest().getPath().getParent());
		Path oManifestPath = output.getPath().resolve(iManifestFolderPath).resolve("manifest.html");
		
		try {
			org.jsoup.nodes.Document doc = cleanup(parse(input.getManifest()));
			Files.write(oManifestPath, doc.html().getBytes(outputCharset));
			DefaultFileSet.Builder builder = DefaultFileSet.with(output, asXHTML(oManifestPath));
			// Attach resources
			Path base = input.getManifest().getPath().getParent();
			getResources(doc, base)
				.forEach(v->builder.add(DefaultAnnotatedFile.create(base.resolve(v)), v));
			return builder.build();
		} catch (IOException e) {
			throw new InternalTaskException(e);
		}

	}
	
	private org.jsoup.nodes.Document cleanup(org.jsoup.nodes.Document doc) throws IOException {

		doc.outputSettings()
			.escapeMode(EscapeMode.xhtml)
			.charset(outputCharset)
			.syntax(Syntax.xml);
		//
		doc.getElementsByTag("script").remove();
		doc.getElementsByTag("noscript").remove();
		
		Elements html = doc.getElementsByTag("html");
		html.attr("xmlns", "http://www.w3.org/1999/xhtml");
		String lang = language.orElseGet(()->{
			String l = html.attr("lang");
			if (!"".equals(l)) {
				return l;
			}
			l = html.attr("xml:lang");
			if (!"".equals(l)) {
				return l;
			} else {
				return DEFAULT_LANGUAGE;
			}
		});

		html.attr("lang", lang);
		html.attr("xml:lang", lang);
		Element head = doc.getElementsByTag("head").first();
		if (head.getElementsByAttribute("charset").isEmpty()) {
			head.appendChild(new Element("meta").attr("charset", outputCharset));
		}
		return doc;
	}
	
	static org.jsoup.nodes.Document parse(AnnotatedFile input) throws IOException {
		try (InputStream in = Files.newInputStream(input.getPath())) {
			return Jsoup.parse(in,
				null,
				input.getPath().getParent().toUri().toASCIIString());
		}
	}
	
	static List<String> getResources(org.jsoup.nodes.Document doc, Path baseFolder) {
		return Stream.concat(
				doc.getElementsByTag("img").stream().map((Element v)->v.attr("src")), 
				doc.getElementsByAttribute("href").stream().map((Element v)->v.attr("href"))
			)
			.filter(src-> {
				try {
					Path r = baseFolder.resolve(src);
					if (r.toFile().exists()) {
						return true;
					} else {
						if (LOGGER.isLoggable(Level.FINE)) {
							LOGGER.fine("File does not exist: " + r);
						}
						return false;
					}
				} catch (InvalidPathException e) {
					return false;
				} 
			})
			.collect(Collectors.toList());
	}
	
	private static AnnotatedFile asXHTML(Path p) {
		return new DefaultAnnotatedFile.Builder(p)
			.extension("xhtml")
			.mediaType("application/xhtml+xml")
			.build();
	}

	private static synchronized List<UserOption> getOptionsInternal() {
		if (options==null) {
			options = new ArrayList<>();
			options.add(new UserOption.Builder(SOURCE_LANGUAGE).description("The primary language of the input file").defaultValue(DEFAULT_LANGUAGE).build());
		}
		return options;		
	}
	
	@Override
	public List<UserOption> getOptions() {
		return getOptionsInternal();
	}
}

package org.daisy.dotify.impl.input.docs;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.daisy.dotify.docs.impl.OdtTask;
import org.daisy.streamline.api.media.DefaultAnnotatedFile;
import org.daisy.streamline.api.tasks.InternalTaskException;
import org.junit.Test;

public class OdtTaskTest {

	@Test
	public void test_01() throws IOException, InternalTaskException, URISyntaxException {
		Map<String, Object> params = new HashMap<>();
		params.put("source-language", "en");
		OdtTask mt = new OdtTask(params);
		File out = File.createTempFile("test", ".tmp");
		out.deleteOnExit();
		mt.execute(DefaultAnnotatedFile.with(Paths.get(this.getClass().getResource("resource-files/input.odt").toURI())).build(), out);
		List<String> actual = normalize(Files.readAllLines(out.toPath()));
		List<String> expected = normalize(Files.readAllLines(
						Paths.get(this.getClass().getResource("resource-files/odt-expected.html").toURI())));
		assertEquals(expected, actual);
	}
	
	private static List<String> normalize(List<String> in) {
		return in.stream().map(s -> s.trim()).collect(Collectors.toList());
	}
}

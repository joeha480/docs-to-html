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

import org.daisy.streamline.api.tasks.InternalTaskException;
import org.junit.Test;

public class DocxTaskTest {

	@Test
	public void test_01() throws IOException, InternalTaskException, URISyntaxException {
		Map<String, Object> params = new HashMap<>();
		params.put("source-language", "en");
		DocxTask mt = new DocxTask(params);
		File out = File.createTempFile("test", ".tmp");
		out.deleteOnExit();
		mt.execute(new File(this.getClass().getResource("resource-files/input.docx").toURI()), out);
		List<String> actual = normalize(Files.readAllLines(out.toPath()));
		List<String> expected = normalize(Files.readAllLines(
						Paths.get(this.getClass().getResource("resource-files/docx-expected.html").toURI())));
		assertEquals(expected, actual);
	}
	
	private static List<String> normalize(List<String> in) {
		return in.stream().map(s -> s.trim()).collect(Collectors.toList());
	}
}

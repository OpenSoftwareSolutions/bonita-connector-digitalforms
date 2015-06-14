package org.oss.digitalforms.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.oss.digitalforms.utils.dom.FormFieldExtractor;
import org.oss.digitalforms.utils.dom.FormTemplateFiller;
import org.oss.digitalforms.utils.map.CaseInsensitiveMap;
import org.w3c.dom.Document;


public class TemplateFillerTest {

	private final static String PROTOCOLKIND = "protocolKind";
	@Test
	public void fillTemplate() throws FileNotFoundException, Exception {
		File template = new File("src/test/resources/digireport-bonita-poc-part1-v0-instance.xml");
		Map<String,String> fillingParameters = new CaseInsensitiveMap();
		fillingParameters.put("quelle", "Werbung");
		fillingParameters.put("Name", "Muster");
		fillingParameters.put("vorname", "Peter");
		FormTemplateFiller filler = new FormTemplateFiller(template);
		filler.fillTemplate(fillingParameters);
		Document filledDocument = filler.getDocument();
		FormFieldExtractor extractor = new FormFieldExtractor(filledDocument);
		Map<String,String> extractedFields = extractor.extractFields();
		removeUnused(extractedFields,fillingParameters.keySet());

		assertEquals(fillingParameters,extractedFields);

	}

	private void removeUnused(Map<String,String> result, Set<String> source) {
		for (Iterator<Map.Entry<String,String>> iter = result.entrySet().iterator(); iter.hasNext();) {
			Map.Entry<String,String> entry = iter.next();
			if (!source.contains(entry.getKey())) {
				iter.remove();
			}
		}
	}

	@Test
	public void folderNameFiller() throws FileNotFoundException, Exception {
		File template = new File("src/test/resources/digireport-bonita-poc-part1-v0-instance.xml");
		FormTemplateFiller filler = new FormTemplateFiller(template);
		String folder = filler.getRootAttributeValue(PROTOCOLKIND);
		assertEquals("bonita-poc-part1",folder);

	}

	@Test
	public void folderNameExtractor() throws FileNotFoundException, Exception {
		File template = new File("src/test/resources/digireport-bonita-poc-part1-v0-instance.xml");
		FormFieldExtractor extractor = new FormFieldExtractor(template);
		String folder = extractor.getRootAttributeValue(PROTOCOLKIND);
		assertEquals("bonita-poc-part1",folder);

	}

}

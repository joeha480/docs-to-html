package org.daisy.dotify.impl.input.docs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.daisy.streamline.api.tasks.InternalTask;
import org.daisy.streamline.api.tasks.TaskGroup;
import org.daisy.streamline.api.tasks.TaskGroupSpecification;
import org.daisy.streamline.api.tasks.TaskSystemException;

public class DocsGroup implements TaskGroup {
	private final TaskGroupSpecification specification;
	
	DocsGroup(TaskGroupSpecification specification) {
		this.specification = specification;
	}

	@Override
	public List<InternalTask> compile(Map<String, Object> parameters)
			throws TaskSystemException {
		List<InternalTask> ret = new ArrayList<>();
		if ("docx".equalsIgnoreCase(specification.getInputType().getIdentifier())) {
			ret.add(new DocxTask(parameters));
		} else if ("odt".equalsIgnoreCase(specification.getInputType().getIdentifier())) {
			ret.add(new OdtTask(parameters));
		} else if ("html".equalsIgnoreCase(specification.getInputType().getIdentifier())) {
			ret.add(new HtmlTask(parameters));
		}
		return ret;
	}

}
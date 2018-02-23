package cd.go.contrib.elasticagents.docker.views;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class ViewBuilder {
    private static ViewBuilder builder;
    private final Configuration configuration;

    private ViewBuilder() {
        configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setTemplateLoader(new ClassTemplateLoader(getClass(), "/"));
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setDateTimeFormat("iso");
    }

    public Template getTemplate(String template) throws IOException {
        return configuration.getTemplate(template);
    }

    public String build(Template template, Object model) throws IOException, TemplateException {
        Writer writer = new StringWriter();
        template.process(model, writer);
        return writer.toString();
    }

    public static ViewBuilder instance() {
        if (builder == null) {
            builder = new ViewBuilder();
        }
        return builder;
    }
}

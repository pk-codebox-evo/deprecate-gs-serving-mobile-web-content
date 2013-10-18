package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mobile.device.view.LiteDeviceDelegatingViewResolver;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.spring3.view.ThymeleafViewResolver;

@Configuration
public class WebConfiguration {

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Bean
    public LiteDeviceDelegatingViewResolver thymeleafViewResolver() {
        ThymeleafViewResolver delegate = new ThymeleafViewResolver();
        delegate.setTemplateEngine(this.templateEngine);
        delegate.setCharacterEncoding("UTF-8");
        LiteDeviceDelegatingViewResolver resolver = new LiteDeviceDelegatingViewResolver(delegate);
        resolver.setMobilePrefix("mobile/");
        resolver.setTabletPrefix("tablet/");
        return resolver;
    }

}

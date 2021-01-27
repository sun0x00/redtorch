package xyz.redtorch.node.master.web.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class WebConfigAdaptor implements WebMvcConfigurer {

    private final String apiBasePath;
    @Autowired
    LoginInterceptor loginInterceptor;

    public WebConfigAdaptor(@Value("${rt.master.apiBasePath}") String apiBasePath) {
        this.apiBasePath = apiBasePath;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**").excludePathPatterns("/", apiBasePath + "/login", apiBasePath + "/logout");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // All resources go to where they should go
        registry.addResourceHandler(
//        		"/**" //
                "/**/*.css", //
                "/**/*.html", //
                "/**/*.js", //
                "/**/*.jsx", //
                "/**/*.json", //
                "/**/*.map", //
                "/**/*.ico", //
                "/**/*.png", //
                "/**/*.ttf", //
                "/**/*.woff", //
                "/**/*.woff2" //
        ).setCachePeriod(0).addResourceLocations("classpath:/static/ReactSPA/").addResourceLocations("/static/ReactSPA/distPRD/");

        registry.addResourceHandler("/", "/**").setCachePeriod(0).addResourceLocations("classpath:/static/ReactSPA/index.html").addResourceLocations("/static/ReactSPA/index.html").resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        if (resourcePath.startsWith(apiBasePath) || resourcePath.startsWith(apiBasePath.substring(1))) {
                            return null;
                        }
                        return location.exists() && location.isReadable() ? location : null;
                    }
                });
    }

}
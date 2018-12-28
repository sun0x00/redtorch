package xyz.redtorch.web.adaptor;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import xyz.redtorch.web.interceptor.AuthInterceptor;

@Configuration
public class WebConfigAdaptor implements WebMvcConfigurer {
	
	private final String baseApiPath;

	public WebConfigAdaptor(@Value("${api.base.path}") String baseApiPath) {
	   this.baseApiPath = baseApiPath;
	}
	
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor());
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
      // All resources go to where they should go
      registry
        .addResourceHandler(
//        		"/**"
        		"/**/*.css",
        		"/**/*.html",
        		"/**/*.js",
        		"/**/*.jsx",
        		"/**/*.png",
        		"/**/*.ttf",
        		"/**/*.woff",
        		"/**/*.woff2"
        		)
        .setCachePeriod(0)
        .addResourceLocations("classpath:/static/ReactSPA/distPRD/")
        .addResourceLocations("/static/ReactSPA/distPRD/");

      registry.addResourceHandler("/", "/**")
        .setCachePeriod(0)
        .addResourceLocations("classpath:/static/ReactSPA/distPRD/index.html")
        .addResourceLocations("/static/ReactSPA/distPRD/index.html")
        .resourceChain(true)
        .addResolver(new PathResourceResolver() {
          @Override
          protected Resource getResource(String resourcePath, Resource location) throws IOException {
            if (resourcePath.startsWith(baseApiPath) || resourcePath.startsWith(baseApiPath.substring(1))) {
              return null;
            }

            return location.exists() && location.isReadable() ? location : null;
          }
        });
      
    }
}

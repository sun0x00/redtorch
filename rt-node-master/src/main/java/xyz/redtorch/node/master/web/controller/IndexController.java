package xyz.redtorch.node.master.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 
 * @author sun0x00@gmail.com
 *
 */
@Controller
public class IndexController {

	@RequestMapping("/")
	public String greeting() {
		return "forward:/index.html";
	}

}

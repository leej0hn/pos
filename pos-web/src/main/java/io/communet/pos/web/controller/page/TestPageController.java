package io.communet.pos.web.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>function:
 * <p>User: LeeJohn
 * <p>Date: 2016/10/14
 * <p>Version: 1.0
 */
@Controller
@RequestMapping("/pos/page/")
public class TestPageController {

	@GetMapping("/test")
	public ModelAndView testPage() {
		String test = "Test Page , hello world ";
		return new ModelAndView("testPage","test",test);
	}


}

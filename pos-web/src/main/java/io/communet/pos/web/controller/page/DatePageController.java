package io.communet.pos.web.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>function:
 * <p>User: LeeJohn
 * <p>Date: 2016/10/14
 * <p>Version: 1.0
 */
@Controller
@RequestMapping("/pos/page/")
public class DatePageController {

	@GetMapping("/date")
	public ModelAndView datePage(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("datePage");
	}


}

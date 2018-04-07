package techit.rest.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import techit.model.Ticket;
import techit.model.User;
import techit.model.dao.TicketDao;
import techit.model.dao.UserDao;
import techit.security.SecurityUtils;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:techit-servlet.xml", "classpath:applicationContext.xml" })
public class TicketControllerTest extends AbstractTransactionalTestNGSpringContextTests{

	@Autowired
	private WebApplicationContext wac;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private TicketDao ticketDao;

	private MockMvc mockMvc;

	@BeforeClass
	void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}
	
	@Test
	void getTicketsPass() throws Exception{
		
		this.mockMvc.perform(get("/tickets")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUtils.createJwtToken(userDao.getUser(2L))))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(1));
	}
	
	@Test
	void getTicketsFail() throws Exception{
		
		this.mockMvc.perform(get("/tickets")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUtils.createJwtToken(userDao.getUser(6L))))
		.andExpect(MockMvcResultMatchers.status().is(403));
	}
	
	@Test
	void addTicketPass() throws Exception {
		
		this.mockMvc.perform(post("/tickets")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUtils.createJwtToken(userDao.getUser(4L)))
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content("{\"subject\" : \"PC Repair\"," 
						+ "\"priority\" : \"LOW\"}"))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty());
	}
	
	@Test
	void getTicketPass() throws Exception{
		
		this.mockMvc.perform(get("/tickets/{ticketId}",3)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUtils.createJwtToken(userDao.getUser(4L))))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("$.subject").isNotEmpty());
	}
	
	@Test
	void getTicketFail() throws Exception{
		
		this.mockMvc.perform(get("/tickets/{ticketId}",2)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUtils.createJwtToken(userDao.getUser(4L))))
		.andExpect(MockMvcResultMatchers.status().is(403));
	}
	
	@Test
	void editTicketPass() throws Exception {
		
		Ticket edit = ticketDao.getTicket(4L);
		edit.setSubject("PC Repair");
		
		this.mockMvc.perform(put("/tickets/{ticketId}",4)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUtils.createJwtToken(userDao.getUser(2L)))
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(new ObjectMapper().writeValueAsString(edit)))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(4))
		.andExpect(MockMvcResultMatchers.jsonPath("$.subject").value("PC Repair"));
	}
	
	@Test
	void editTicketFail() throws Exception {
		
		Ticket edit = ticketDao.getTicket(4L);
		edit.setSubject("PC Repair");
		
		this.mockMvc.perform(put("/tickets/{ticketId}",4)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUtils.createJwtToken(userDao.getUser(4L)))
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(new ObjectMapper().writeValueAsString(edit)))
		.andExpect(MockMvcResultMatchers.status().is(403));
	}
	
//	@Test
//	void getTicketTechniciansPass() throws Exception{
//		
//		this.mockMvc.perform(get("/tickets/{ticketId}/technicians",1)
//				.header(HttpHeaders.AUTHORIZATION, "Bearer " + SecurityUtils.createJwtToken(userDao.getUser(1L))))
//		.andExpect(MockMvcResultMatchers.status().isOk())
//		.andExpect(MockMvcResultMatchers.jsonPath("$.length()",2));
//	}
}

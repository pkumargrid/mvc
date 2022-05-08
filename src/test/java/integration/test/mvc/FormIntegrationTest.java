package integration.test.mvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import integration.test.mvc.cadidate.Candidate;
import integration.test.mvc.cadidate.CandidateRepository;

@SpringBootTest
@AutoConfigureMockMvc
class FormIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CandidateRepository candidateRepository;

	@Test
	@WithMockUser(username = "jonh", password = "pass", roles = {"USER"})
	public void givenNonAdminUserWhenTryAddCandidateAForbiddenResponseIsGotten() throws Exception {
		mockMvc.perform(get("/add"))
		.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username="seth", password = "pass", roles = {"ADMIN"})
	public void adminUserCanAddACandidate() throws Exception{
		mockMvc.perform(post("/add")
				.param("name", "Mark")
				.param("score", "5")
				)
		.andExpect(status().is3xxRedirection())
		.andExpect(model().hasNoErrors())
		.andExpect(view().name("redirect:/"));

		Candidate candidate = candidateRepository
				.findOne(Example.of(new Candidate("Mark"))).get();

		Assertions.assertThat(candidate.getName()).isEqualTo("Mark");
	}

	@Test
	@WithMockUser(username="admin", password = "pass", roles = {"ADMIN"})
	public void givenInvalidFieldValuesErrorsMustBeDisplayed () throws Exception {
		mockMvc.perform(post("/add")
				.param("name", "")
				.param("score", "15"))
		.andExpect(status().isOk())
		.andExpect(view().name("add"))
		.andExpect(model().attribute("candidate", Matchers.any(Candidate.class)))
		.andExpect(model().hasErrors())
		.andExpect(model().errorCount(2))
		.andExpect(model().attributeHasErrors("candidate"))
		.andExpect(model().attributeHasFieldErrors("candidate", "score"))
		.andExpect(model().attributeHasFieldErrorCode("candidate", "name", "NotBlank"));

	}


}

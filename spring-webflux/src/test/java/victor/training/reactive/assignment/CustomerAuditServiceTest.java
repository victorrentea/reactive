package victor.training.reactive.assignment;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;
import victor.training.reactive.Utils;
import victor.training.util.WireMockExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodName.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerAuditServiceTest {
   @InjectMocks
   private CustomerAuditService service;
   @Mock
   private EmailSender emailSender;
   @Mock
   private CustomerReactiveRepo repo;

   @RegisterExtension
   public WireMockExtension wireMock = new WireMockExtension(9999);

   private TestPublisher<Customer> customerRepoMono = TestPublisher.<Customer>createCold();
   private TestPublisher<Void> sendInactiveEmailMono = TestPublisher.<Void>createCold();

   @BeforeEach
   public void initMocks() {
      wireMock.stubFor(WireMock.any(anyUrl()).willReturn(aResponse().withStatus(200))); // override the /mappings jsons
      when(repo.findById(1)).thenReturn(customerRepoMono.mono());
      when(emailSender.sendInactiveCustomerEmail(any())).thenReturn(sendInactiveEmailMono.mono());
      service.setBaseUrl("http://localhost:9999");
   }
   @Test
   public void p1_callsRepo() {
      service.processOrderingCustomer(Flux.just(1));
      assertThat(customerRepoMono.subscribeCount()).isEqualTo(1);
   }
   @Test
   public void p2_sendsEmail_forInactive() {
      service.processOrderingCustomer(Flux.just(1));
      customerRepoMono.emit(new Customer(1, false));
      assertThat(sendInactiveEmailMono.subscribeCount()).isEqualTo(1);
   }
   @Test
   public void p2_doesntSendEmail_forActive() {
      service.processOrderingCustomer(Flux.just(1));
      customerRepoMono.emit(new Customer(1, true));
      assertThat(sendInactiveEmailMono.subscribeCount()).isEqualTo(0);
   }
   @Test
   public void p3_auditsCustomer_whenActive() {
      service.processOrderingCustomer(Flux.just(1));
      customerRepoMono.emit(new Customer(1, true));
      Utils.sleep(1000);
      wireMock.verify(getRequestedFor(urlEqualTo("/customer-audit/1")));
   }

   @Test
   public void p3_doesntAuditsCustomer_whenInactive() {
      service.processOrderingCustomer(Flux.just(1));
      customerRepoMono.emit(new Customer(1, false));
      Utils.sleep(1000);
      wireMock.verify(0, getRequestedFor(urlEqualTo("/customer-audit/1")));
   }
   @Test
   public void p4_retrieveExternalClient() {
      wireMock.stubFor(WireMock.any(urlEqualTo("/external-customer/1,3"))
          .willReturn(aResponse().withStatus(200)
              .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
              .withBody("[{\n" +
                        "  \"id\": 1,\n" +
                        "  \"active\": true\n" +
                        "},{\n" +
                        "  \"id\": 3,\n" +
                        "  \"active\": false\n" +
                        "}]")));


      when(repo.findById(1)).thenReturn(Mono.just(new Customer(1, false, true)));
      when(repo.findById(2)).thenReturn(Mono.just(new Customer(2, true, false)));
      when(repo.findById(3)).thenReturn(Mono.just(new Customer(3, false, true)));

      service.processOrderingCustomer(Flux.just(1,2,3));

      Utils.sleep(1000);

      wireMock.verify(getRequestedFor(urlEqualTo("/external-customer/1,3")));


      ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
      Mockito.verify(emailSender).sendInactiveCustomerEmail(customerCaptor.capture());

      Assertions.assertThat(customerCaptor.getAllValues().stream().map(Customer::getId)).containsExactly(3);
      assertThat(sendInactiveEmailMono.subscribeCount()).isEqualTo(1);
   }


}
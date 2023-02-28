package codes.bespoke.brastak.phones.controller;

import java.time.OffsetDateTime;
import java.util.List;

import codes.bespoke.brastak.phones.mapper.PhoneMapperImpl;
import codes.bespoke.brastak.phones.mapper.SpecMapperImpl;
import codes.bespoke.brastak.phones.model.Phone;
import codes.bespoke.brastak.phones.model.PhoneModel;
import codes.bespoke.brastak.phones.model.Reservation;
import codes.bespoke.brastak.phones.model.Spec;
import codes.bespoke.brastak.phones.service.PhoneService;
import codes.bespoke.brastak.phones.service.ReservationService;
import codes.bespoke.brastak.phones.service.SpecService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest
@ContextConfiguration(classes = { PhoneController.class, PhoneMapperImpl.class, SpecMapperImpl.class })
public class PhoneControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PhoneService phoneService;
    @MockBean
    private ReservationService reservationService;
    @MockBean
    private SpecService specService;

    private final Phone phone;
    private final Spec spec;
    private final Reservation reservation;

    public PhoneControllerTest() {
        phone = new Phone();
        phone.setId(1);
        phone.setSerialNumber("1001");

        PhoneModel model = new PhoneModel();
        model.setId(1);
        model.setVendor("Test");
        model.setName("Test phone");
        phone.setModel(model);

        spec = new Spec();
        spec.setId(1);
        spec.setPhoneModel(model);
        spec.setTechnologies(List.of("GSM"));
        spec.set_2gBands(List.of("GSM_900", "GSM_1800"));
        model.setSpec(spec);

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setPhone(phone);
        reservation.setReservedAt(OffsetDateTime.now());
        reservation.setReservedBy("brastak");
    }

    @Test
    public void testGetAllPhones() throws Exception {
        Mockito.when(phoneService.findAll()).thenReturn(List.of(phone));
        Mockito.when(specService.specEnrich(Mockito.any())).thenReturn(spec);
        Mockito.when(reservationService.findReservationsByPhoneIds(Mockito.any()))
            .thenReturn(List.of(reservation));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/phones/all"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].serialNumber").value("1001"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].model").value("Test phone"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].spec.technologies[0]").value("GSM"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].reservation.reservedBy").value("brastak"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.[0].reservation.reservedAt").isNotEmpty());

        Mockito.verify(specService).specEnrich(phone.getModel());
        Mockito.verify(reservationService).findReservationsByPhoneIds(List.of(1));
    }
}

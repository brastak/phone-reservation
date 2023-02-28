package codes.bespoke.brastak.phones.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest
@ContextConfiguration(classes = { ReservationController.class, PhoneMapperImpl.class, SpecMapperImpl.class })
public class ReservationControllerTest {
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

    public ReservationControllerTest() {
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
    public void testReservePhone_success() throws Exception {
        Mockito.when(phoneService.findUnreservedPhone(Mockito.anyInt()))
            .thenReturn(Optional.of(phone));
        Mockito.when(reservationService.reservePhone(Mockito.any(), Mockito.anyString()))
            .thenReturn(reservation);
        Mockito.when(specService.specEnrich(Mockito.any())).thenReturn(spec);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"modelId\": 1, \"reservedBy\": \"brastak\"}"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.serialNumber").value("1001"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.model").value("Test phone"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.spec.technologies[0]").value("GSM"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.reservation.reservedBy").value("brastak"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.reservation.reservedAt").isNotEmpty());

        Mockito.verify(phoneService).findUnreservedPhone(1);
        Mockito.verify(reservationService).reservePhone(phone, "brastak");
        Mockito.verify(specService).specEnrich(phone.getModel());
    }

    @Test
    public void testReservePhone_phoneNotFound() throws Exception {
        Mockito.when(phoneService.findUnreservedPhone(Mockito.anyInt()))
            .thenReturn(Optional.empty());

        this.mockMvc.perform(MockMvcRequestBuilders.post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"modelId\": 1, \"reservedBy\": \"brastak\"}"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(phoneService).findUnreservedPhone(1);
        Mockito.verifyNoMoreInteractions(reservationService, specService);
    }

    @Test
    public void testFinishReservation_success() throws Exception {
        Mockito.when(reservationService.findActiveReservationById(Mockito.anyLong()))
            .thenReturn(Optional.of(reservation));
        Mockito.doNothing().when(reservationService).finishReservation(Mockito.any());

        this.mockMvc.perform(MockMvcRequestBuilders.post("/reservations/1/finish"))
            .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(reservationService).findActiveReservationById(1);
        Mockito.verify(reservationService).finishReservation(reservation);
    }

    @Test
    public void testFinishReservation_reservationNotFound() throws Exception {
        Mockito.when(reservationService.findActiveReservationById(Mockito.anyLong()))
            .thenReturn(Optional.empty());
        Mockito.doNothing().when(reservationService).finishReservation(Mockito.any());

        this.mockMvc.perform(MockMvcRequestBuilders.post("/reservations/1/finish"))
            .andExpect(MockMvcResultMatchers.status().isNotFound());

        Mockito.verify(reservationService).findActiveReservationById(1);
        Mockito.verifyNoMoreInteractions(reservationService);
    }
}

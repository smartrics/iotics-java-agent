package smartrics.iotics.space;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IoticSpaceTest {

    @Mock
    public ServiceRegistry mockServiceRegistry;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void whenInitialisedFindsHostEndpointsFromServiceRegistry() throws IOException {
        HostEndpoints e = newEndpoints();
        when(mockServiceRegistry.find()).thenReturn(e);
        new IoticSpace(mockServiceRegistry).initialise();
        verify(mockServiceRegistry).find();
    }


    private HostEndpoints newEndpoints() {
        return new HostEndpoints("resolver", "stomp", "qapi", "grpc", "grpcWeb", new HostEndpointVersion(
                "1", "2", "3", "4", "5"
        ));
    }
}
// All calls go through the Spring Cloud API Gateway, never directly to a microservice.
export const environment = {
  /** API Gateway base URL. /api/users/** is routed to service-utilisateurs. */
  apiUrl: 'http://localhost:9090',
  /**
   * Messaging WebSocket (Socket.IO) endpoint. Connects DIRECTLY to service-messaging
   * (real-time sockets through the Spring Cloud Gateway are fiddly). REST history for
   * messaging still goes through the gateway at apiUrl/api/messages.
   */
  messagingWsUrl: 'http://localhost:8085',
};

// All calls go through the Spring Cloud API Gateway, never directly to a microservice.
export const environment = {
  /**
   * API Gateway base URL — EVERYTHING goes through here: users, events, reservations,
   * avis, and messaging (both REST /api/messages and the Socket.IO WebSocket /socket.io).
   */
  apiUrl: 'http://localhost:9090',
};

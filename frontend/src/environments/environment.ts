// All calls go through the Spring Cloud API Gateway, never directly to a microservice.
export const environment = {
  /** API Gateway base URL. /api/users/** is routed to service-utilisateurs. */
  apiUrl: 'http://localhost:9090',
};

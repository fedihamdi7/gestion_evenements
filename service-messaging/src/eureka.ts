import { Eureka } from 'eureka-js-client';

/**
 * Registers this Node service in the Eureka registry so the Spring Cloud API
 * Gateway can route to it with lb://service-messaging, exactly like the Java
 * services. EUREKA_HOST defaults to localhost, overridden to "eureka-server" in Docker.
 */
export function registerWithEureka(): Eureka {
  const eurekaHost = process.env.EUREKA_HOST || 'localhost';
  const port = Number(process.env.PORT || 8085);
  const host = process.env.INSTANCE_HOST || 'localhost';
  const ip = process.env.INSTANCE_IP || '127.0.0.1';

  const client = new Eureka({
    instance: {
      app: 'service-messaging',
      instanceId: `service-messaging:${port}`,
      hostName: host,
      ipAddr: ip,
      vipAddress: 'service-messaging',
      secureVipAddress: 'service-messaging',
      statusPageUrl: `http://${host}:${port}/health`,
      healthCheckUrl: `http://${host}:${port}/health`,
      port: { $: port, '@enabled': true },
      dataCenterInfo: {
        '@class': 'com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo',
        name: 'MyOwn',
      },
    },
    eureka: {
      host: eurekaHost,
      port: 8761,
      servicePath: '/eureka/apps/',
      maxRetries: 10,
      requestRetryDelay: 3000,
    },
  });

  client.logger.level('warn');
  client.start((err: Error) => {
    if (err) console.error('Eureka registration failed:', err.message);
    else console.log('Registered with Eureka as service-messaging');
  });

  // De-register cleanly on shutdown.
  process.on('SIGINT', () => client.stop(() => process.exit()));
  process.on('SIGTERM', () => client.stop(() => process.exit()));

  return client;
}

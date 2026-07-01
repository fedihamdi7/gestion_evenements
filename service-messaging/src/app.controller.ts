import { Controller, Get } from '@nestjs/common';

@Controller()
export class AppController {
  // Simple health endpoint used by Eureka's healthCheckUrl.
  @Get('health')
  health() {
    return { status: 'UP', service: 'service-messaging' };
  }
}

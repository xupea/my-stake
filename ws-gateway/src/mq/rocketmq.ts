import { SimpleConsumer } from 'rocketmq-client-nodejs';
import { MQConsumer, MessageHandler } from './index';
import config from '../config';

export class RocketMQConsumer extends MQConsumer {
  private consumer: SimpleConsumer | null = null;
  private running = false;

  async connect(): Promise<void> {
    this.consumer = new SimpleConsumer({
      endpoints: config.rocketmq.endpoints,
      // 阿里云实例命名空间，开源版为空字符串
      namespace: config.rocketmq.namespace,
      consumerGroup: config.rocketmq.consumerGroup,
      // 订阅在构造时传入，topic -> tag 表达式
      subscriptions: new Map([[config.rocketmq.topic, config.rocketmq.tag || '*']]),
      ...(config.rocketmq.accessKey ? {
        sessionCredentials: {
          accessKey: config.rocketmq.accessKey,
          accessSecret: config.rocketmq.accessSecret,
        },
      } : {}),
    });
    await this.consumer.startup();
    console.log(`[RocketMQ] connected, endpoints: ${config.rocketmq.endpoints}`);
  }

  async subscribe(handler: MessageHandler): Promise<void> {
    if (!this.consumer) throw new Error('RocketMQ not connected');
    this.running = true;
    this.poll(handler);
  }

  private async poll(handler: MessageHandler): Promise<void> {
    while (this.running) {
      try {
        const messages = await this.consumer!.receive(20);
        for (const msg of messages) {
          try {
            const content = msg.body.toString();
            const message = JSON.parse(content);
            await handler(message);
            await this.consumer!.ack(msg);
          } catch (err) {
            console.error('[RocketMQ] handler error:', (err as Error).message);
          }
        }
      } catch (err) {
        if (this.running) {
          console.error('[RocketMQ] receive error:', (err as Error).message);
          await new Promise(r => setTimeout(r, 1000));
        }
      }
    }
  }

  async close(): Promise<void> {
    this.running = false;
    await this.consumer?.shutdown();
  }
}

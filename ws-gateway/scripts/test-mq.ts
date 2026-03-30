/**
 * 阿里云 RocketMQ 连接验证脚本
 * 用法：
 *   ROCKETMQ_ENDPOINTS=xxx ROCKETMQ_NAMESPACE=xxx ... npx ts-node scripts/test-mq.ts
 * 或在 .env 里配好后直接：
 *   npx ts-node scripts/test-mq.ts
 */
import dotenv from 'dotenv';
dotenv.config();

import { SimpleConsumer, Producer } from 'rocketmq-client-nodejs';

const endpoints  = process.env.ROCKETMQ_ENDPOINTS   || 'localhost:8081';
const namespace  = process.env.ROCKETMQ_NAMESPACE    || '';
const group      = process.env.ROCKETMQ_CONSUMER_GROUP || 'ws-gateway-group';
const topic      = process.env.ROCKETMQ_TOPIC        || 'stake-ws-push';
const accessKey  = process.env.ROCKETMQ_ACCESS_KEY   || '';
const accessSecret = process.env.ROCKETMQ_ACCESS_SECRET || '';

const credentials = accessKey
  ? { sessionCredentials: { accessKey, accessSecret } }
  : {};

async function testProducer() {
  console.log('\n[Producer] 测试发送...');
  const producer = new Producer({
    endpoints,
    namespace,
    ...credentials,
  });

  await producer.startup();
  console.log('[Producer] 连接成功 ✅');

  const receipt = await producer.send({
    topic,
    tag: 'test',
    body: Buffer.from(JSON.stringify({
      type: 'broadcast',
      payload: { text: 'hello from test script' },
      timestamp: Date.now(),
    })),
  });
  console.log('[Producer] 发送成功 ✅, messageId:', receipt.messageId);

  await producer.shutdown();
}

async function testConsumer() {
  console.log('\n[Consumer] 测试接收（等待 10s）...');
  const consumer = new SimpleConsumer({
    endpoints,
    namespace,
    consumerGroup: group,
    subscriptions: new Map([[topic, '*']]),
    ...credentials,
  });

  await consumer.startup();
  console.log('[Consumer] 连接成功 ✅');

  const messages = await consumer.receive(10);
  if (messages.length === 0) {
    console.log('[Consumer] 队列暂无消息（正常，说明连接没问题）✅');
  } else {
    console.log(`[Consumer] 收到 ${messages.length} 条消息 ✅`);
    for (const msg of messages) {
      console.log('  body:', msg.body.toString());
      await consumer.ack(msg);
    }
  }

  await consumer.shutdown();
}

async function main() {
  console.log('=== RocketMQ 连接验证 ===');
  console.log('endpoints :', endpoints);
  console.log('namespace :', namespace || '(空，开源版)');
  console.log('topic     :', topic);
  console.log('group     :', group);
  console.log('auth      :', accessKey ? '已配置 AccessKey' : '无（开源版）');

  try {
    // await testProducer();
    await testConsumer();
    console.log('\n✅ 全部验证通过，RocketMQ 连接正常\n');
  } catch (err) {
    console.error('\n❌ 验证失败:', (err as Error).message);
    console.error('\n常见原因：');
    console.error('  1. ROCKETMQ_ENDPOINTS 格式错误，阿里云格式为 xxx.rmq.aliyuncs.com:8080');
    console.error('  2. ROCKETMQ_NAMESPACE 未填或填错（阿里云必填，格式 MQ_INST_xxx）');
    console.error('  3. AccessKey / AccessSecret 错误');
    console.error('  4. Topic 或 ConsumerGroup 在控制台未创建');
    console.error('  5. IP 白名单未放开当前机器\n');
    process.exit(1);
  }
}

main();

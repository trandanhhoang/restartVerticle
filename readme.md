# readme

- rabbitMQProducer

  - config.json
  - MainProducer.java (deployer)
    - Deploy and send over eventBus here
  - RabbitMqProducerService.java (gateway)
    - Consumer message from deployer and map name with ID and name with config
    - Undeploy and deploy when be called.

- Because restart deploy need "config" and "ID" and "short_name" so we need map 3 of them with each orther by 2 map<String,Object>.

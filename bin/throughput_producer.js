/**
 * Created by atrimble on 4/21/2015.
 */

var bigio = require('../bigio/bigio');
var logger = require('winston');

var message = {
    sendTime: 0,
    padding: 'a'
};

var throwAway = 10000;
var sampleSize = 100000;
var warmedUp = false;
var messageCount = 0;

var startTime, endTime;

function printStats() {
    var time = endTime - startTime;
    var seconds = time / 1000;
    var bandwidth = messageCount / seconds;

    logger.info('bytes , messages , duration ,messages/s\n');
    logger.info(' unk  , ' + messageCount + ', ' + seconds + ', ' + bandwidth);
}

bigio.initialize(function() {
    bigio.addListener({
        topic: 'ThroughputProducer',
        listener: function(message) {
            if (messageCount >= throwAway && !warmedUp) {
                warmedUp = true;
                messageCount = 0;
                startTime = new Date();
            }

            messageCount++;

            if (messageCount > sampleSize) {
                endTime = new Date();

                printStats();
                messageCount = 0;
                warmedUp = false;

                logger.info('Done');
            } else {
                bigio.send({
                    topic: 'ThroughputConsumer',
                    message: message
                });
            }
        }
    });

    setTimeout(function() {
        logger.info('Sending start message.');
        bigio.send({
            topic: 'ThroughputConsumer',
            message: message
        });
    }, 2000);
});

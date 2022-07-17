package io.github.athingx.athing.platform.mock;

import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;

public class MockMessageConsumer implements MessageConsumer {

    private volatile MessageListener listener;

    @Override
    public String getMessageSelector(){
        throw new UnsupportedOperationException();
    }

    @Override
    public MessageListener getMessageListener(){
        return listener;
    }

    @Override
    public void setMessageListener(MessageListener listener){
        this.listener = listener;
    }

    @Override
    public Message receive(){
        throw new UnsupportedOperationException();
    }

    @Override
    public Message receive(long timeout){
        throw new UnsupportedOperationException();
    }

    @Override
    public Message receiveNoWait(){
        throw new UnsupportedOperationException();
    }

    @Override
    public void close(){

    }

}


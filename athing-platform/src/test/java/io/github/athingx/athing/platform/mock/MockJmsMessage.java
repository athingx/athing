package io.github.athingx.athing.platform.mock;

import jakarta.jms.Destination;
import jakarta.jms.Message;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MockJmsMessage implements Message {

    private String messageId = UUID.randomUUID().toString();
    private final Map<String, Object> propertyMap = new HashMap<>();
    private final byte[] body;

    public MockJmsMessage(byte[] body) {
        this.body = body;
    }

    @Override
    public String getJMSMessageID() {
        return messageId;
    }

    @Override
    public void setJMSMessageID(String id) {
        this.messageId = id;
    }

    @Override
    public long getJMSTimestamp() {
        return 0;
    }

    @Override
    public void setJMSTimestamp(long timestamp) {

    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() {
        return new byte[0];
    }

    @Override
    public void setJMSCorrelationIDAsBytes(byte[] correlationID) {

    }

    @Override
    public void setJMSCorrelationID(String correlationID) {

    }

    @Override
    public String getJMSCorrelationID() {
        return null;
    }

    @Override
    public Destination getJMSReplyTo() {
        return null;
    }

    @Override
    public void setJMSReplyTo(Destination replyTo) {

    }

    @Override
    public Destination getJMSDestination() {
        return null;
    }

    @Override
    public void setJMSDestination(Destination destination) {

    }

    @Override
    public int getJMSDeliveryMode() {
        return 0;
    }

    @Override
    public void setJMSDeliveryMode(int deliveryMode) {

    }

    @Override
    public boolean getJMSRedelivered() {
        return false;
    }

    @Override
    public void setJMSRedelivered(boolean redelivered) {

    }

    @Override
    public String getJMSType() {
        return null;
    }

    @Override
    public void setJMSType(String type) {

    }

    @Override
    public long getJMSExpiration() {
        return 0;
    }

    @Override
    public void setJMSExpiration(long expiration) {

    }

    @Override
    public long getJMSDeliveryTime() {
        return 0;
    }

    @Override
    public void setJMSDeliveryTime(long deliveryTime) {

    }

    @Override
    public int getJMSPriority() {
        return 0;
    }

    @Override
    public void setJMSPriority(int priority) {

    }

    @Override
    public void clearProperties() {

    }

    @Override
    public boolean propertyExists(String name) {
        return false;
    }

    @Override
    public boolean getBooleanProperty(String name) {
        return false;
    }

    @Override
    public byte getByteProperty(String name) {
        return 0;
    }

    @Override
    public short getShortProperty(String name) {
        return 0;
    }

    @Override
    public int getIntProperty(String name) {
        return 0;
    }

    @Override
    public long getLongProperty(String name) {
        return 0;
    }

    @Override
    public float getFloatProperty(String name) {
        return 0;
    }

    @Override
    public double getDoubleProperty(String name) {
        return 0;
    }

    @Override
    public String getStringProperty(String name) {
        return (String) propertyMap.get(name);
    }

    @Override
    public Object getObjectProperty(String name) {
        return null;
    }

    @Override
    public Enumeration<?> getPropertyNames() {
        return null;
    }

    @Override
    public void setBooleanProperty(String name, boolean value) {

    }

    @Override
    public void setByteProperty(String name, byte value) {

    }

    @Override
    public void setShortProperty(String name, short value) {

    }

    @Override
    public void setIntProperty(String name, int value) {

    }

    @Override
    public void setLongProperty(String name, long value) {

    }

    @Override
    public void setFloatProperty(String name, float value) {

    }

    @Override
    public void setDoubleProperty(String name, double value) {

    }

    @Override
    public void setStringProperty(String name, String value) {
        propertyMap.put(name,value);
    }

    @Override
    public void setObjectProperty(String name, Object value) {

    }

    @Override
    public void acknowledge() {

    }

    @Override
    public void clearBody() {

    }

    @Override
    public <T> T getBody(Class<T> c) {
        if (c.equals(byte[].class)) {
            return (T) body;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBodyAssignableTo(Class c) {
        return false;
    }

    public static MockJmsMessage message(String messageId, String topic, String body) {
        final MockJmsMessage message = new MockJmsMessage(body.getBytes(UTF_8));
        message.setJMSMessageID(messageId);
        message.setStringProperty("topic", topic);
        return message;
    }

}

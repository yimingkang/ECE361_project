CC=javac
OBJS=Listener.java CCClient.java RoutingClient.java MainClass.java

all:
	$(CC) $(OBJS)

clean:
	rm -f *class

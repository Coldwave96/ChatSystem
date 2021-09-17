# Chat System

This is a simple "chat" application, using the C/S architectural model.

## Usage
```
# server defaults to 4444
java -jar chatserver.jar

# OR accept custom port with flag -p
java -jar chatserver.jar -p 6666

# client takes hostname as first arg and defaults to 4444
java -jar chatclient.jar localhost

# OR takes hostname as first arg AND accepts port argument with flag -p
java -jar chatclient.jar localhost -p 6666
```

## Commands
```
#identitychange [new identity]
#join [room]
#who [room]
#list
#createroom [room]
#deleteroom [room]
#quit
```
## Protocols

Please go [here](https://coldwave96.github.io/2021/09/17/ChatSystem/) for more information of protocols.

## Wire

### Types

```json
identity: [a-zA-Z0-9]{3,16}
string: char[]
i32: int
```

### C2S

#### identitychange

```json
{
    "type": "identitychange",
    "identity": identity
}
```

#### join

```json
{
    "type":"join",
    "roomid": string
}
```


#### who

```json
{
    "type":"who",
    "roomid": string
}
```

#### list

```json
{
    "type":"list"
}
```

#### createroom

```json
{
    "type":"createroom",
    "roomid": string
}
```

#### delete

```json
{
    "type":"delete",
    "roomid": string
}
```

#### quit

```json
{
    "type":"quit"
}
```

#### message


```json
{
    "type":"message",
    "content": string
}
```

### S2C


#### newidentity

```json
{
    "type":"newidentity",
    "former": identity,
    "identity": identity
}
````

#### roomchange

```json
{
    "type":"roomchange",
    "identity": identity,
    "former": identity,
    "roomid": string
}
```

#### roomcontents

```json
{
    "type":"roomcontents",
    "roomid": string,
    "identities": [identity],
    "owner": identity
}
```

#### roomlist

```json
{
    "type":"roomlist",
    "rooms": [{"roomid": string, "count": i32}]
}
```

#### message

```json
{
    "type":"message",
    "identity": identity,
    "content": string
}
```
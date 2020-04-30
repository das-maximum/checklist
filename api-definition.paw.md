# API

## Requests

### **GET** - /api/todo

#### CURL

```sh
curl -X GET "http://localhost:8081/api/todo"
```

### **GET** - /api/todo

#### CURL

```sh
curl -X GET "http://localhost:8081/api/todo\
?id=a9f15316-30eb-429b-9e7a-695f8287a33a"
```

#### Query Parameters

- **id** should respect the following schema:

```
{
  "type": "string",
  "enum": [
    "a9f15316-30eb-429b-9e7a-695f8287a33a"
  ],
  "default": "a9f15316-30eb-429b-9e7a-695f8287a33a"
}
```

### **GET** - /api/todo/full

#### CURL

```sh
curl -X GET "http://localhost:8081/api/todo/full"
```

### **POST** - /api/todo

#### CURL

```sh
curl -X POST "http://localhost:8081/api/todo" \
    -H "Content-Type: text/plain; charset=utf-8" \
    --data-raw "$body"
```

#### Header Parameters

- **Content-Type** should respect the following schema:

```
{
  "type": "string",
  "enum": [
    "text/plain; charset=utf-8"
  ],
  "default": "text/plain; charset=utf-8"
}
```

#### Body Parameters

- **body** should respect the following schema:

```
{
  "type": "string",
  "default": "id=1234"
}
```

### **PUT** - /api/todo

#### CURL

```sh
curl -X PUT "http://localhost:8081/api/todo" \
    -H "Content-Type: application/json; charset=utf-8" \
    --data-raw "$body"
```

#### Header Parameters

- **Content-Type** should respect the following schema:

```
{
  "type": "string",
  "enum": [
    "application/json; charset=utf-8"
  ],
  "default": "application/json; charset=utf-8"
}
```

#### Body Parameters

- **body** should respect the following schema:

```
{
  "type": "string",
  "default": "{\"id\":\"a9f15316-30eb-429b-9e7a-695f8287a33a\",\"text\":\"id=1234\",\"done\":true,\"created\":\"2020-04-29T19:21:56.905774Z\"}"
}
```

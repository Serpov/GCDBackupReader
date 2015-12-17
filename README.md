# GCDBackupReader
RecordReadChannel implementation for BlobstoreService

```java
final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
final BlobKey blobKey = blobstoreService.createGsBlobKey("/gs/" + bucket + "/" + pathToOutputFile);

final RecordReadChannel rrc = BlobserviceHelper.openRecordReadChannel(blobKey, blobstoreService);

ByteBuffer bf;
while ((bf = rrc.readRecord()) != null) {
  final OnestoreEntity.EntityProto proto = new OnestoreEntity.EntityProto();
  proto.mergeFrom(bf.array());
  final Entity entity = EntityTranslator.createFromPb(proto);
  entity.removeProperty(""); // Remove empty property
  //Now you can save entity to datastore or read keys and properties
}
```

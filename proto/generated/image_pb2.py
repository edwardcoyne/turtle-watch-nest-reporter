# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: image.proto

import sys
_b=sys.version_info[0]<3 and (lambda x:x) or (lambda x:x.encode('latin1'))
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor.FileDescriptor(
  name='image.proto',
  package='com.islandturtlewatch.nest.data',
  syntax='proto2',
  serialized_options=_b('B\nImageProtoH\003'),
  serialized_pb=_b('\n\x0bimage.proto\x12\x1f\x63om.islandturtlewatch.nest.data\"C\n\x08ImageRef\x12\x10\n\x08owner_id\x18\x01 \x01(\t\x12\x11\n\treport_id\x18\x02 \x01(\x04\x12\x12\n\nimage_name\x18\x03 \x01(\t\"W\n\x0eImageUploadRef\x12\x38\n\x05image\x18\x01 \x01(\x0b\x32).com.islandturtlewatch.nest.data.ImageRef\x12\x0b\n\x03url\x18\x02 \x01(\t\"Y\n\x10ImageDownloadRef\x12\x38\n\x05image\x18\x01 \x01(\x0b\x32).com.islandturtlewatch.nest.data.ImageRef\x12\x0b\n\x03url\x18\x02 \x01(\tB\x0e\x42\nImageProtoH\x03')
)




_IMAGEREF = _descriptor.Descriptor(
  name='ImageRef',
  full_name='com.islandturtlewatch.nest.data.ImageRef',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='owner_id', full_name='com.islandturtlewatch.nest.data.ImageRef.owner_id', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='report_id', full_name='com.islandturtlewatch.nest.data.ImageRef.report_id', index=1,
      number=2, type=4, cpp_type=4, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='image_name', full_name='com.islandturtlewatch.nest.data.ImageRef.image_name', index=2,
      number=3, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=48,
  serialized_end=115,
)


_IMAGEUPLOADREF = _descriptor.Descriptor(
  name='ImageUploadRef',
  full_name='com.islandturtlewatch.nest.data.ImageUploadRef',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='image', full_name='com.islandturtlewatch.nest.data.ImageUploadRef.image', index=0,
      number=1, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='url', full_name='com.islandturtlewatch.nest.data.ImageUploadRef.url', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=117,
  serialized_end=204,
)


_IMAGEDOWNLOADREF = _descriptor.Descriptor(
  name='ImageDownloadRef',
  full_name='com.islandturtlewatch.nest.data.ImageDownloadRef',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='image', full_name='com.islandturtlewatch.nest.data.ImageDownloadRef.image', index=0,
      number=1, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
    _descriptor.FieldDescriptor(
      name='url', full_name='com.islandturtlewatch.nest.data.ImageDownloadRef.url', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto2',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=206,
  serialized_end=295,
)

_IMAGEUPLOADREF.fields_by_name['image'].message_type = _IMAGEREF
_IMAGEDOWNLOADREF.fields_by_name['image'].message_type = _IMAGEREF
DESCRIPTOR.message_types_by_name['ImageRef'] = _IMAGEREF
DESCRIPTOR.message_types_by_name['ImageUploadRef'] = _IMAGEUPLOADREF
DESCRIPTOR.message_types_by_name['ImageDownloadRef'] = _IMAGEDOWNLOADREF
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

ImageRef = _reflection.GeneratedProtocolMessageType('ImageRef', (_message.Message,), dict(
  DESCRIPTOR = _IMAGEREF,
  __module__ = 'image_pb2'
  # @@protoc_insertion_point(class_scope:com.islandturtlewatch.nest.data.ImageRef)
  ))
_sym_db.RegisterMessage(ImageRef)

ImageUploadRef = _reflection.GeneratedProtocolMessageType('ImageUploadRef', (_message.Message,), dict(
  DESCRIPTOR = _IMAGEUPLOADREF,
  __module__ = 'image_pb2'
  # @@protoc_insertion_point(class_scope:com.islandturtlewatch.nest.data.ImageUploadRef)
  ))
_sym_db.RegisterMessage(ImageUploadRef)

ImageDownloadRef = _reflection.GeneratedProtocolMessageType('ImageDownloadRef', (_message.Message,), dict(
  DESCRIPTOR = _IMAGEDOWNLOADREF,
  __module__ = 'image_pb2'
  # @@protoc_insertion_point(class_scope:com.islandturtlewatch.nest.data.ImageDownloadRef)
  ))
_sym_db.RegisterMessage(ImageDownloadRef)


DESCRIPTOR._options = None
# @@protoc_insertion_point(module_scope)

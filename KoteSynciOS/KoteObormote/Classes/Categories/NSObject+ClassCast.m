
#import "NSObject+ClassCast.h"

@implementation NSObject (ClassCast)

- (id)castOrNil:(Class)clz {
   if ([self isKindOfClass:clz]) {
      return self;
   }
   return nil;
}


- (id)castToProtocolOrNil:(Protocol * )protocol {
   if ([self conformsToProtocol:protocol]) {
      return self;
   }
   return nil;
}


- (id)castOrNilWithNullCheck:(Class)clz {
   if ([self isKindOfClass:clz] && (id)clz != [NSNull null]) {
      return self;
   }
   return nil;
}


@end

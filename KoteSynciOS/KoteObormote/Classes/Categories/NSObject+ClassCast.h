
#import <Foundation/Foundation.h>

@interface NSObject (ClassCast)

- (id)castOrNil:(Class)clz;

- (id)castToProtocolOrNil:(Protocol * )protocol;

- (id)castOrNilWithNullCheck:(Class)clz;

@end

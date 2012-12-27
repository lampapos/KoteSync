
#import "NSMutableDictionary+NotNilValue.h"


@implementation NSMutableDictionary (NotNilValue)

- (void)setObjectIfNotNil:(id)value forKey:(id)key {
   if (value) {
      [self setObject:value forKey:key];
   }
}

@end